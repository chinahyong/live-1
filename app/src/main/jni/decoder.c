#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>
#include <libswresample/swresample.h>

#include <librtmp/rtmp.h>

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#include <jni.h>
#include <assert.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <jni.h>
#include <string.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#define LOG_TAG "android-ffmpeg-tutorial02"
#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);

ANativeWindow* window;
char *videoFileName;
AVFormatContext *formatCtx = NULL;
int videoStream, audioStream;
AVCodecContext *codecCtx = NULL, *auCodecCtx;
AVFrame *decodedFrame = NULL, *frameRGBA = NULL, *audioFrame = NULL;
jobject bitmap;
void* buffer;
struct SwsContext *sws_ctx = NULL, *au_sws_ctx = NULL;
int width;
int height;
int stop;
uint8_t * streamBuf;
int audioBufSize = 0;
char* audioBuf, *tmpAudioBuf;
int tmpBufOffset;
unsigned char * iobuffer;

RTMP * _rtmp;
JNIEnv* mEnv, *mThreadEnv;
jclass mThreadClass;
//RTMPPacket rtmp_pakt;
char* streming_url_;
JavaVM *vm;
struct envObj {
	jobject obj;
	JNIEnv* env;
	jclass klass;
};
char* rtmpUrl;

// engine interfaces
SLObjectItf engineObject = NULL;
SLEngineItf engineEngine;

// output mix interfaces
SLObjectItf outputMixObject = NULL;
SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;

// buffer queue player interfaces
SLObjectItf bqPlayerObject = NULL;
SLPlayItf bqPlayerPlay;
SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
SLEffectSendItf bqPlayerEffectSend;
SLMuteSoloItf bqPlayerMuteSolo;
SLVolumeItf bqPlayerVolume;

// aux effect on the output mix, used by the buffer queue player
const SLEnvironmentalReverbSettings reverbSettings =
		SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

// file descriptor player interfaces
SLObjectItf fdPlayerObject = NULL;
SLPlayItf fdPlayerPlay;
SLSeekItf fdPlayerSeek;
SLMuteSoloItf fdPlayerMuteSolo;
SLVolumeItf fdPlayerVolume;

// pointer and size of the next player buffer to enqueue, and number of remaining buffers
short *nextBuffer;
unsigned nextSize;
int nextCount;

// this callback handler is called every time a buffer finishes playing
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	assert(bq == bqPlayerBufferQueue);
	assert(NULL == context);
	// for streaming playback, replace this test by logic to find and fill the next buffer
	if (--nextCount > 0 && NULL != nextBuffer && 0 != nextSize) {
		SLresult result;
		// enqueue another buffer
		result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue,
				nextBuffer, nextSize);
		// the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
		// which for this code example would indicate a programming error
		assert(SL_RESULT_SUCCESS == result);
	}
}

void createEngine(JNIEnv* env, jclass clazz) {
	SLresult result;

	// create engine
	result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
	assert(SL_RESULT_SUCCESS == result);

	// realize the engine
	result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);

	// get the engine interface, which is needed in order to create other objects
	result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
			&engineEngine);
	assert(SL_RESULT_SUCCESS == result);

	// create output mix, with environmental reverb specified as a non-required interface
	const SLInterfaceID ids[1] = { SL_IID_ENVIRONMENTALREVERB };
	const SLboolean req[1] = { SL_BOOLEAN_FALSE };
	result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1,
			ids, req);
	assert(SL_RESULT_SUCCESS == result);

	// realize the output mix
	result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);

	// get the environmental reverb interface
	// this could fail if the environmental reverb effect is not available,
	// either because the feature is not present, excessive CPU load, or
	// the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
	result = (*outputMixObject)->GetInterface(outputMixObject,
			SL_IID_ENVIRONMENTALREVERB, &outputMixEnvironmentalReverb);
	if (SL_RESULT_SUCCESS == result) {
		result =
				(*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
						outputMixEnvironmentalReverb, &reverbSettings);
	}
	// ignore unsuccessful result codes for environmental reverb, as it is optional for this example
}

void createBufferQueueAudioPlayer(JNIEnv* env, jclass clazz, int rate,
		int channel, int fmt) {
	SLresult result;

	// configure audio source
//	SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
//			SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2 };
//    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_16,
//        SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
//        SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN};
	SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
			SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2 };
	SLDataFormat_PCM format_pcm = { SL_DATAFORMAT_PCM, channel, rate * 1000,
			fmt, fmt,
					channel == 2 ?
							SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT :
							SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN };
//	SLDataFormat_PCM format_pcm;
//	format_pcm.formatType = SL_DATAFORMAT_PCM;
//	format_pcm.numChannels = channel;
//	format_pcm.samplesPerSec = rate * 1000;
//	format_pcm.bitsPerSample = bitsPerSample;
//	format_pcm.containerSize = 16;
//	if (channel == 2)
//		format_pcm.channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
//	else
//		format_pcm.channelMask = SL_SPEAKER_FRONT_CENTER;
//	format_pcm.endianness = SL_BYTEORDER_LITTLEENDIAN;
	SLDataSource audioSrc = { &loc_bufq, &format_pcm };

	// configure audio sink
	SLDataLocator_OutputMix loc_outmix = { SL_DATALOCATOR_OUTPUTMIX,
			outputMixObject };
	SLDataSink audioSnk = { &loc_outmix, NULL };

	// create audio player
	const SLInterfaceID ids[3] = { SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND,
	/*SL_IID_MUTESOLO,*/SL_IID_VOLUME };
	const SLboolean req[3] = { SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE,
	/*SL_BOOLEAN_TRUE,*/SL_BOOLEAN_TRUE };
	result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject,
			&audioSrc, &audioSnk, 3, ids, req);
	assert(SL_RESULT_SUCCESS == result);
// realize the player
	result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);

	// get the play interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY,
			&bqPlayerPlay);
	assert(SL_RESULT_SUCCESS == result);

	// get the buffer queue interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
			&bqPlayerBufferQueue);
	assert(SL_RESULT_SUCCESS == result);

	// register callback on the buffer queue
	result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue,
			bqPlayerCallback, NULL);
	assert(SL_RESULT_SUCCESS == result);

	// get the effect send interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_EFFECTSEND,
			&bqPlayerEffectSend);
	assert(SL_RESULT_SUCCESS == result);

#if 0   // mute/solo is not supported for sources that are known to be mono, as this is
	// get the mute/solo interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_MUTESOLO, &bqPlayerMuteSolo);
	assert(SL_RESULT_SUCCESS == result);
#endif

	// get the volume interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME,
			&bqPlayerVolume);
	assert(SL_RESULT_SUCCESS == result);

// set the player's state to playing
	result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
	assert(SL_RESULT_SUCCESS == result);

}

void AudioWrite(const void*buffer, int size) {
	(*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, buffer, size);
}

int read_stream_buf(void *opaque, uint8_t* buf, int buf_size) {
	int read_size = RTMP_Read(_rtmp, (char*) buf, buf_size);
	return read_size;
}

jstring sToJstring(JNIEnv* env, const char* pat) {
	return (*env)->NewStringUTF(env, pat);
}

static void invokeHandler(const char *body, unsigned int nBodySize,
		struct RTMP *_rtmp) {
//	char *copyBody = malloc(nBodySize);
//	if (!copyBody) {
//		LOGI("invokeHandler malloc1 error");
//		return;
//	}
//
//	memcpy(copyBody, body, nBodySize);
//
//	AMFObject *obj = malloc(sizeof(AMFObject));
//	if (!obj) {
//		LOGI("invokeHandler malloc2 error");
//		return;
//	}
//	if (AMF_Decode(obj, body, nBodySize, FALSE) < 0) {
//		LOGI("invokeHandler AMF_Decode error");
//		return;
//	}
//
//	LOGI(AMFObject_STRING(obj, 0));
//
//	if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onInitRoom") == 0) {
//		AMFObject *sobj = AMFObject_OBJECT(obj, 5);
//		int i = 0;
//		for (; i < sobj->o_num; i++) {
//			AMFObject *item = AMFObject_OBJECT(sobj, i);
//			int j = 0;
//			char *uid, *sex, *type, *nickname, *photo, *vip_end, *vip_start;
//			for (; j < item->o_num; j++) {
//				char name[256];
//				strncpy(name, item->o_props[j].p_name.av_val,
//						item->o_props[j].p_name.av_len);
//				name[item->o_props[j].p_name.av_len] = 0;
//				if (strcmp(name, "headPic") == 0) {
//					photo = AMFObject_STRING(item, j);
//				} else if (strcmp(name, "nickname") == 0) {
//					nickname = AMFObject_STRING(item, j);
//				} else if (strcmp(name, "type") == 0) {
//					type = AMFObject_STRING(item, j);
//				} else if (strcmp(name, "vip_start") == 0) {
//					vip_start = AMFObject_STRING(item, j);
//				} else if (strcmp(name, "vip_end") == 0) {
//					vip_end = AMFObject_STRING(item, j);
//				} else if (strcmp(name, "sex") == 0) {
//					sex = AMFObject_STRING(item, j);
//				} else if (strcmp(name, "uid") == 0) {
//					uid = AMFObject_STRING(item, j);
//				}
//			}
//			LOGI("FINDING CLASS");
//			jclass ffmpegClass = (jclass)(*mEnv)->FindClass(mEnv,
//					"com/lonzh/lib/LZFFmpeg");
//			LOGI("found FFmpeg Class");
//			jmethodID mid =
//					(*mEnv)->GetStaticMethodID(mEnv, ffmpegClass, "addUser",
//							"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
//			LOGI("found addUser Method");
//			(*mEnv)->CallStaticVoidMethod(mEnv, ffmpegClass, mid,
//					sToJstring(mEnv, uid), sToJstring(mEnv, type),
//					sToJstring(mEnv, sex), sToJstring(mEnv, nickname),
//					sToJstring(mEnv, photo), sToJstring(mEnv, vip_start),
//					sToJstring(mEnv, vip_end));
//		}
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onCheckVideo") == 0) {
//		AMFObject *sobj = AMFObject_OBJECT(obj, 5);
//		int isPlaying = AMFObject_NUMBER(sobj, 0);
//		// Call Back Java
////		jclass ffmpegClass = (jclass)(*env)->FindClass(env,
////				"com/lonzh/lib/LZFFmpeg");
////		jmethodID mid = (*env)->GetStaticMethodID(env, ffmpegClass,
////				"onCheckVideo", "(Z)V");
////		jboolean lIsPlaying = (*env)->CallStaticObjectMethod(env, ffmpegClass,
////				mid, (jboolean) isPlaying);
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onLogin") == 0) {
//		AMFObject *sobj = AMFObject_OBJECT(obj, 5);
//		char *nickname, *photo, *vip_end, *vip_start, *type, *sex, *uid;
//		int i = 0;
//		for (; i < sobj->o_num; i++) {
//			char name[256];
//			strncpy(name, sobj->o_props[i].p_name.av_val,
//					sobj->o_props[i].p_name.av_len);
//			name[sobj->o_props[i].p_name.av_len] = 0;
//			if (strcmp(name, "headPic") == 0) {
//				photo = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "nickname") == 0) {
//				nickname = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "type") == 0) {
//				type = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "vip_start") == 0) {
//				vip_start = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "vip_end") == 0) {
//				vip_end = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "sex") == 0) {
//				sex = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "uid") == 0) {
//				uid = AMFObject_STRING(sobj, i);
//			}
//		}
//
//		(*vm)->AttachCurrentThread(vm, &mThreadEnv, NULL);
//		jmethodID mid =
//				(*mThreadEnv)->GetStaticMethodID(mThreadEnv, mThreadClass,
//						"addUser",
//						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
//		LOGI("found addUser Method");
//		(*mThreadEnv)->CallStaticVoidMethod(mThreadEnv, mThreadClass, mid,
//				sToJstring(mThreadEnv, uid), sToJstring(mThreadEnv, type),
//				sToJstring(mThreadEnv, sex), sToJstring(mThreadEnv, nickname),
//				sToJstring(mThreadEnv, photo),
//				sToJstring(mThreadEnv, vip_start),
//				sToJstring(mThreadEnv, vip_end));
//		(*vm)->DetachCurrentThread(vm);
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onLogout") == 0) {
//		AMFObject *sobj = AMFObject_OBJECT(obj, 5);
//		char* uid, *type;
//		int i = 0;
//		for (; i < sobj->o_num; i++) {
//			char name[256];
//			strncpy(name, sobj->o_props[i].p_name.av_val,
//					sobj->o_props[i].p_name.av_len);
//			name[sobj->o_props[i].p_name.av_len] = 0;
//			if (strcmp(name, "uid") == 0) {
//				uid = AMFObject_STRING(sobj, i);
//				if (type != NULL)
//					break;
//			} else if (strcmp(name, "type") == 0) {
//				type = AMFObject_STRING(sobj, i);
//				if (uid != NULL)
//					break;
//			}
//		}
//		(*vm)->AttachCurrentThread(vm, &mThreadEnv, NULL);
//		jmethodID mid = (*mThreadEnv)->GetStaticMethodID(mThreadEnv,
//				mThreadClass, "onLogout",
//				"(Ljava/lang/String;Ljava/lang/String;)V");
//		(*mThreadEnv)->CallStaticVoidMethod(mThreadEnv, mThreadClass, mid,
//				sToJstring(mThreadEnv, uid), sToJstring(mThreadEnv, type));
//		(*vm)->DetachCurrentThread(vm);
////		int uid = AMFObject_NUMBER(sobj, 0);
////		char* nickname = AMFObject_STRING(sobj, 2);
////		LOGI("%s LOGGED OUT", nickname);
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onChatMsg") == 0) {
//		AMFObject *sobj = AMFObject_OBJECT(obj, 5);
//		int i = 0;
//		char *from, *to, *fromPhoto, *fromNickname, *msg, *toPhoto, *toNickname;
//		int isPrivate = -100;
//		for (; i < sobj->o_num; i++) {
//			char name[256];
//			strncpy(name, sobj->o_props[i].p_name.av_val,
//					sobj->o_props[i].p_name.av_len);
//			name[sobj->o_props[i].p_name.av_len] = 0;
//			if (strcmp(name, "from") == 0) {
//				from = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "fromHeadPic") == 0) {
//				fromPhoto = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "fromNickname") == 0) {
//				fromNickname = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "msg") == 0) {
//				fromPhoto = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "private") == 0) {
//				isPrivate = AMFObject_NUMBER(sobj, i);
//			} else if (strcmp(name, "to") == 0) {
//				to = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "toHeadPic") == 0) {
//				toPhoto = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "toNickname") == 0) {
//				toNickname = AMFObject_STRING(sobj, i);
//			}
//		}
//		(*vm)->AttachCurrentThread(vm, &mThreadEnv, NULL);
//		jmethodID mid =
//				(*mThreadEnv)->GetStaticMethodID(mThreadEnv, mThreadClass,
//						"onChatMsg",
//						"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
//		LOGI("found onChatMsg Method");
//		(*mThreadEnv)->CallStaticVoidMethod(mThreadEnv, mThreadClass, mid,
//				sToJstring(mThreadEnv, from), sToJstring(mThreadEnv, to),
//				sToJstring(mThreadEnv, fromNickname),
//				sToJstring(mThreadEnv, toNickname),
//				sToJstring(mThreadEnv, fromPhoto),
//				sToJstring(mThreadEnv, toPhoto), isPrivate);
//		(*vm)->DetachCurrentThread(vm);
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onGiftMsg") == 0) {
//		AMFObject *sobj = AMFObject_OBJECT(obj, 5);
//		int i = 0;
//		char *from, *to, *fromPhoto, *fromNickname, *toPhoto, *toNickname,
//				*giftName, *giftImg;
//		int giftCount, giftId;
//		for (; i < sobj->o_num; i++) {
//			char name[256];
//			strncpy(name, sobj->o_props[i].p_name.av_val,
//					sobj->o_props[i].p_name.av_len);
//			name[sobj->o_props[i].p_name.av_len] = 0;
//			if (strcmp(name, "from") == 0) {
//				from = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "fromHeadPic") == 0) {
//				fromPhoto = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "fromNickname") == 0) {
//				fromNickname = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "propId") == 0) {
//				giftId = AMFObject_NUMBER(sobj, i);
//			} else if (strcmp(name, "count") == 0) {
//				giftCount = AMFObject_NUMBER(sobj, i);
//			} else if (strcmp(name, "propPic") == 0) {
//				giftImg = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "propName") == 0) {
//				giftName = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "to") == 0) {
//				to = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "toHeadPic") == 0) {
//				toPhoto = AMFObject_STRING(sobj, i);
//			} else if (strcmp(name, "toNickname") == 0) {
//				toNickname = AMFObject_STRING(sobj, i);
//			}
//		}
//		(*vm)->AttachCurrentThread(vm, &mThreadEnv, NULL);
//		jmethodID mid =
//				(*mThreadEnv)->GetStaticMethodID(mThreadEnv, mThreadClass,
//						"onGift",
//						"(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
//		(*mThreadEnv)->CallStaticVoidMethod(mThreadEnv, mThreadClass, mid,
//				giftId, giftCount, sToJstring(mThreadEnv, from),
//				sToJstring(mThreadEnv, to),
//				sToJstring(mThreadEnv, fromNickname),
//				sToJstring(mThreadEnv, toNickname),
//				sToJstring(mThreadEnv, fromPhoto),
//				sToJstring(mThreadEnv, toPhoto),
//				sToJstring(mThreadEnv, giftName),
//				sToJstring(mThreadEnv, giftImg));
//		(*vm)->DetachCurrentThread(vm);
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onPublish") == 0) {
//
//	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
//			&& strcmp(AMFObject_STRING(obj, 0), "onUnpublish") == 0) {
//		stop = 1;
//	}
//	AMF_Reset(obj);
//	free(copyBody);
}

jint naInitRtmp(JNIEnv *pEnv, jobject pObj, jstring pUrl) {
	// Init Rtmp
	mEnv = pEnv;
	mThreadEnv = pEnv;
	mThreadClass = (*pEnv)->FindClass(pEnv, "com/lonzh/lib/LZFFmpeg");
	_rtmp = RTMP_Alloc();
	LOGI("Rtmp Alloc success");
	RTMP_Init(_rtmp);
	_rtmp->Link.timeout = 5;
	LOGI("Rtmp Init success");
	RTMP_SetupInvokeHandler(_rtmp, invokeHandler);
	LOGI("RTMP_SetupInvokeHandler success");
	streming_url_ = (char*) malloc(256);
	char* _url = (char *) (*pEnv)->GetStringUTFChars(pEnv, pUrl, NULL);
	strcpy(streming_url_, _url);
	int err = RTMP_SetupURL(_rtmp, streming_url_);
	if (err <= 0)
		return -1;
	LOGI("RTMP_SetupURL success");
	err = RTMP_Connect(_rtmp, NULL);
	if (err <= 0)
		return -1;
	LOGI("RTMP_Connect success");
//	err = RTMP_ConnectStream(_rtmp, 0);
//	if (err <= 0)
//		return -1;
//	LOGI("RTMP_ConnectStream success");

// onCheckVideo
//	AMFObjectProperty data;
//	data.p_name.av_val = NULL;
//	data.p_name.av_len = 0;
//	data.p_type = AMF_OBJECT;
//	RTMP_SendCall(_rtmp, "checkVideo", &data);
//	LOGI("RTMP call checkVideo success");

//	RTMP_Read(_rtmp, buf, 32768);
//	free(buf);

	return 0;
}

// Send Msg
jint naSendMsg(JNIEnv *pEnv, jobject pOjb, jstring psTo, jstring psMsg,
		jboolean pbPrivate) {
	AMFObjectProperty data;
	data.p_name.av_val = NULL;
	data.p_name.av_len = 0;
	data.p_type = AMF_OBJECT;
	LOGI("BUILD data success");

	AMFObject amfObj;
	AMFObjectProperty props[3];
	props[0].p_name.av_val = "msg";
	props[0].p_name.av_len = 3;
	props[0].p_type = AMF_STRING;
	AVal val0;
	val0.av_val = (char *) (*pEnv)->GetStringUTFChars(pEnv, psMsg, NULL);
	val0.av_len = strlen(val0.av_val);
	props[0].p_vu.p_aval = val0;

	props[1].p_name.av_val = "to";
	props[1].p_name.av_len = 2;
	props[1].p_type = AMF_STRING;
	AVal val1;
	val1.av_val = (char *) (*pEnv)->GetStringUTFChars(pEnv, psTo, NULL);
	val1.av_len = strlen(val1.av_val);
	props[1].p_vu.p_aval = val1;

	props[2].p_name.av_val = "private";
	props[2].p_name.av_len = 7;
	props[2].p_type = AMF_BOOLEAN;
	props[2].p_vu.p_number = (pbPrivate == JNI_TRUE ? 1 : 0);

	amfObj.o_num = 3;
	amfObj.o_props = props;

	data.p_vu.p_object = amfObj;
	LOGI("BUILD property success");
	RTMP_SendCall(_rtmp, "sendMsg", &data);
}

jint naInitFFmpeg(JNIEnv *pEnv, jobject pObj) {
	AVCodec *pCodec = NULL, *pAuCodec;
	int i;
	AVDictionary *optionsDict = NULL, *auOptionsDict = NULL;
	videoFileName = "";
	LOGI("video file name is %s", videoFileName);
	// Register all formats and codecs
	av_register_all();
	formatCtx = avformat_alloc_context();
	formatCtx->max_analyze_duration = 5 * AV_TIME_BASE;
	unsigned char * iobuffer = (unsigned char *) av_malloc(32768);
	if (streamBuf == NULL)
		streamBuf = (uint8_t *) malloc(32768);
	AVIOContext *avio = avio_alloc_context(iobuffer, 32768, 0, NULL,
			read_stream_buf, NULL, NULL);
	formatCtx->pb = avio;

	// Open video file
	if (avformat_open_input(&formatCtx, videoFileName, NULL, NULL) != 0)
		return -1; // Couldn't open file
	// Retrieve stream information
	if (avformat_find_stream_info(formatCtx, NULL) < 0)
		return -1; // Couldn't find stream information
	// Dump information about file onto standard error
	av_dump_format(formatCtx, -1, videoFileName, 0);
	// Find the first video stream
	videoStream = -1;
	audioStream = -1;
	for (i = 0; i < formatCtx->nb_streams; i++) {
		if (formatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			videoStream = i;
		} else if (formatCtx->streams[i]->codec->codec_type
				== AVMEDIA_TYPE_AUDIO) {
			audioStream = i;
		}
	}
	if (videoStream == -1)
		return -1; // Didn't find a video stream
	if (audioStream == -1) { // Didn't find a audio stream
		return -1;
	}
	// Get a pointer to the codec context for the video stream
	codecCtx = formatCtx->streams[videoStream]->codec;
	auCodecCtx = formatCtx->streams[audioStream]->codec;
	// Find the decoder for the video stream
	pCodec = avcodec_find_decoder(codecCtx->codec_id);
	if (pCodec == NULL) {
		fprintf(stderr, "Unsupported video codec!\n");
		return -1; // Codec not found
	}
	pAuCodec = avcodec_find_decoder(auCodecCtx->codec_id);
	if (pAuCodec == NULL) {
		fprintf(stderr, "Unsupported Audio codec!\n");
		return -1; // Codec not found
	}
	// Open codec
	if (avcodec_open2(codecCtx, pCodec, &optionsDict) < 0)
		return -1; // Could not open codec
	if (avcodec_open2(auCodecCtx, pAuCodec, &auOptionsDict) < 0)
		return -1; // Could not open codec
	// Allocate video frame
	decodedFrame = avcodec_alloc_frame();
	// Allocate an AVFrame structure
	frameRGBA = avcodec_alloc_frame();
	// Allocate audio frame
	audioFrame = avcodec_alloc_frame();
	if (frameRGBA == NULL)
		return -1;
	LOGI(
			"fromwidth=%d fromheight=%d pix_fmt=%d", codecCtx->width, codecCtx->height, codecCtx->pix_fmt);
	createEngine(pEnv, pObj);
	return 0;
}

jintArray naGetVideoRes(JNIEnv *pEnv, jobject pObj) {
	jintArray lRes;
	if (NULL == codecCtx) {
		LOGI("codexCtx is NULL....");
		return NULL;
	}
	lRes = (*pEnv)->NewIntArray(pEnv, 2);
	if (lRes == NULL) {
		LOGI("cannot allocate memory for video size");
		return NULL;
	}
	jint lVideoRes[2];
	lVideoRes[0] = codecCtx->width;
	lVideoRes[1] = codecCtx->height;
	(*pEnv)->SetIntArrayRegion(pEnv, lRes, 0, 2, lVideoRes);
	return lRes;
}

void naSetSurface(JNIEnv *pEnv, jobject pObj, jobject pSurface) {
	if (0 != pSurface) {
		// get the native window reference
		window = ANativeWindow_fromSurface(pEnv, pSurface);
		// set format and size of window buffer
		ANativeWindow_setBuffersGeometry(window, 0, 0, WINDOW_FORMAT_RGBA_8888);
	} else {
		// release the native window
		ANativeWindow_release(window);
		window = NULL;
	}
}

jint naSetup(JNIEnv *pEnv, jobject pObj, int pWidth, int pHeight) {
	width = pWidth;
	height = pHeight;
	sws_ctx = sws_getContext(codecCtx->width, codecCtx->height,
			codecCtx->pix_fmt, pWidth, pHeight, AV_PIX_FMT_RGBA, SWS_BILINEAR,
			NULL, NULL, NULL);
	buffer = malloc(width * height * 4);
	avpicture_fill((AVPicture *) frameRGBA, buffer, AV_PIX_FMT_RGBA, pWidth,
			pHeight);
	return 0;
}

void finish(JNIEnv *pEnv) {
	LOGI("Free the iobuffer");
	if (iobuffer != NULL)
		av_free(iobuffer);
	// Free the RGB image
	LOGI("Free the RGB image");
	av_free(frameRGBA);
	// Free the YUV frame
	LOGI("Free the YUV frame");
	av_free(decodedFrame);
	av_free(audioFrame);
	// Close the codec
	LOGI("Close the codec");
	avcodec_close(codecCtx);
	avcodec_close(auCodecCtx);
	// Close the video file
	LOGI("Close the video file");
	avformat_close_input(&formatCtx);
	LOGI("Free streamBuf");
//	free(streamBuf);
	LOGI("free streming_url_");
	free(streming_url_);
	LOGI("free audioBuf");
	if (audioBuf != NULL)
		free(audioBuf);
	if (tmpAudioBuf != NULL)
		free(tmpAudioBuf);
	LOGI("free buffer");
	if (buffer != NULL)
		free(buffer);
}

void decodeAndRender(JNIEnv* pEnv) {
	ANativeWindow_Buffer windowBuffer;
	AVPacket packet;
	int i = 0;
	int frameFinished;
	int lineCnt;
	int waitTime, baseTime, pts;
	int audioRet;
	int called = 0;

	while (av_read_frame(formatCtx, &packet) >= 0 && !stop) {
		// Is this a packet from the video stream?
		if (packet.stream_index == videoStream) {
			if (window == NULL)
				continue;
			// Decode video frame
//			LOGI("Decode video frame");
			avcodec_decode_video2(codecCtx, decodedFrame, &frameFinished,
					&packet);
			// Did we get a video frame?
			if (frameFinished) {
				// Convert the image from its native format to RGBA
				sws_scale(sws_ctx, (uint8_t *) decodedFrame->data,
						decodedFrame->linesize, 0, codecCtx->height,
						frameRGBA->data, frameRGBA->linesize);
				if (window == NULL)
					continue;
				if (ANativeWindow_lock(window, &windowBuffer, NULL) < 0) {
//					LOGE("cannot lock window");
				} else {
					// draw the frame on buffer
//					LOGI("copy buffer %d:%d:%d", width, height, width*height*4);
//					LOGI(
//							"window buffer: %d:%d:%d", windowBuffer.width, windowBuffer.height, windowBuffer.stride);
					int k = 0;
					for (; k < height; ++k)
						memcpy(windowBuffer.bits + windowBuffer.stride * k * 4,
								frameRGBA->data[0] + width * k * 4, width * 4);
					// unlock the window buffer and post it to display
					ANativeWindow_unlockAndPost(window);
					// count number of frames
					++i;
				}
			}
		} else if (packet.stream_index == audioStream) {
//			LOGI("Decoding Audio...");
			audioRet = avcodec_decode_audio4(auCodecCtx, audioFrame,
					&frameFinished, &packet);
			if (audioRet > 0 && frameFinished) {
//				LOGI("Decode Audio Success!!!");
//				if (au_sws_ctx == NULL) {
//					au_sws_ctx = swr_alloc_set_opts(NULL,
//							av_get_default_channel_layout(auCodecCtx->channels),
//							AV_SAMPLE_FMT_S16, auCodecCtx->sample_rate,
//							av_get_default_channel_layout(auCodecCtx->channels),
//							auCodecCtx->sample_fmt, auCodecCtx->sample_rate, 0,
//							NULL);
//					swr_init(au_sws_ctx);
//				}
//				if (au_sws_ctx) {
//					int data_size = av_samples_get_buffer_size(NULL,
//							auCodecCtx->channels, audioFrame->nb_samples,
//							AV_SAMPLE_FMT_S16, 1);
////					int data_size = av_samples_get_buffer_size(NULL,
////							auCodecCtx->channels, audioFrame->nb_samples,
////							AV_SAMPLE_FMT_S16, 1);
////					LOGI("DataSize=%d", data_size);
//					if (audioBuf == NULL) {
//						audioBuf = malloc(data_size);
//						audioBufSize = data_size;
//					} else if (audioBufSize < data_size) {
//						audioBuf = realloc(audioBuf, data_size);
//						audioBufSize = data_size;
//					}
//					char* outBuf[2] = { audioBuf, 0 };
////					LOGI("START CONVERT....");
//					int outsamples = swr_convert(au_sws_ctx, outBuf,
//							audioFrame->nb_samples,
//							(const uint8_t**) audioFrame->data,
//							audioFrame->nb_samples);
//					if (outsamples < 0) {
//						LOGI("Resample Failed");
//						return;
//					}
////					LOGI("RESAMPLE Success");
////					LOGI(
////							"audio  data_size:%d channels:%d, nb_samples:%d sample_rate:%d sample_fmt:%d", data_size, auCodecCtx->channels, audioFrame->nb_samples, auCodecCtx->sample_rate, auCodecCtx->sample_fmt);
//					if (tmpBufOffset < 8192) {
//						memcpy(tmpAudioBuf + tmpBufOffset, audioBuf, data_size);
//						tmpBufOffset += data_size;
//					} else {
//						(*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue,
//								audioBuf, tmpBufOffset);
//						tmpBufOffset = 0;
//					}

				// Call Back Java
//				if (pEnvObj == NULL) {
//					LOGI("FUCK! The pEnv is NULL")
//				} else {
//					(*vm)->AttachCurrentThread(vm, &(pEnvObj->env), NULL);
//					jclass ffmpegClass = pEnvObj->klass;
//					jmethodID mid = (*(pEnvObj->env))->GetStaticMethodID(
//							pEnvObj->env, ffmpegClass, "onWriteAudioBuf",
//							"([BI)V");
//					(*(pEnvObj->env))->CallStaticObjectMethod(pEnvObj->env,
//											ffmpegClass, mid, audioFrame->data[0], data_size);
//					(*vm)->DetachCurrentThread(vm);
//				}
//
//				(*pEnv)->CallStaticObjectMethod(pEnv,
//						ffmpegClass, mid, audioFrame->data[0], data_size);
//					static uint8_t audio_buf[SWR_CH_MAX];
//					uint8_t *out[] = { audio_buf };
//					int outsamples = swr_convert(au_sws_ctx, out, data_size,
//							(const uint8_t**) audioFrame->extended_data,
//							audioFrame->nb_samples);
//					char *data = (char *) malloc(data_size);
//					short *sample_buffer = (short *) audioFrame->data[0];
//					int liIndex;
//					for (liIndex = 0; liIndex < data_size / 2; liIndex++) {
//						data[liIndex * 2] = (char) (sample_buffer[liIndex / 2]
//								& 0xFF);
//						data[liIndex * 2 + 1] = (char) ((sample_buffer[liIndex
//								/ 2] >> 8) & 0xFF);
//				}

//				}
			} else
				LOGI("Decode Audio FAILED!!!");
//			usleep(5000);
		}
		// Free the packet that was allocated by av_read_frame
//		LOGI("Free Packet...");
		av_free_packet(&packet);
//		LOGI("Free Packet Success");
	}
	LOGI("total No. of frames decoded and rendered %d", i);
	finish(pEnv);
}

/**
 * start the video playback
 */
void naPlay(JNIEnv *pEnv, jobject pObj) {
	//create a new thread for video decode and render
	pthread_t decodeThread;
	stop = 0;
	createBufferQueueAudioPlayer(pEnv, pObj, auCodecCtx->sample_rate,
			auCodecCtx->channels, SL_PCMSAMPLEFORMAT_FIXED_16);
	tmpAudioBuf = malloc(16384);
	tmpBufOffset = 0;
	mThreadEnv = pEnv;
	mThreadClass = (*pEnv)->FindClass(pEnv, "com/lonzh/lib/LZFFmpeg");
	pthread_create(&decodeThread, NULL, decodeAndRender, pEnv);
}

/**
 * stop the video playback
 */
void naStop(JNIEnv *pEnv, jobject pObj) {
	stop = 1;
}

void mainProcess(JNIEnv *pEnv) {
	// 1 init Rtmp

}

void naInit(JNIEnv *pEnv, jobject pObj) {
	pthread_t mainThread;
	pthread_create(&mainThread, NULL, mainProcess, pEnv);
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	vm = pVm;
	JNIEnv * env;
	if ((*pVm)->GetEnv(pVm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}
	JNINativeMethod nm[10];
	nm[0].name = "naInitFFmpeg";
	nm[0].signature = "()I";
	nm[0].fnPtr = (void*) naInitFFmpeg;

	nm[1].name = "naSetSurface";
	nm[1].signature = "(Landroid/view/Surface;)V";
	nm[1].fnPtr = (void*) naSetSurface;

	nm[2].name = "naGetVideoRes";
	nm[2].signature = "()[I";
	nm[2].fnPtr = (void*) naGetVideoRes;

	nm[3].name = "naSetup";
	nm[3].signature = "(II)I";
	nm[3].fnPtr = (void*) naSetup;

	nm[4].name = "naPlay";
	nm[4].signature = "()V";
	nm[4].fnPtr = (void*) naPlay;

	nm[5].name = "naStop";
	nm[5].signature = "()V";
	nm[5].fnPtr = (void*) naStop;

	nm[6].name = "naInitRtmp";
	nm[6].signature = "(Ljava/lang/String;)I";
	nm[6].fnPtr = (void*) naInitRtmp;

	nm[7].name = "naSendMsg";
	nm[7].signature = "(Ljava/lang/String;Ljava/lang/String;Z)I";
	nm[7].fnPtr = (void*) naSendMsg;

	jclass ffmpegClass = (*env)->FindClass(env, "com/lonzh/lib/LZFFmpeg");
//Register methods with env->RegisterNatives.
	(*env)->RegisterNatives(env, ffmpegClass, nm, 8);
	return JNI_VERSION_1_6;
}

