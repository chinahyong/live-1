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
#define DEBUG

// RTMP相关变量
char* rtmpUrl;
char* chatRtmpUrl;

RTMP * _rtmp;
RTMP * _chatRtmp;

JavaVM* jvm;
JNIEnv* mEnv;
jclass mJClass;
// 是否已释放播放资源
int isRelease = 0;
// 是否已释放消息连接资源
int isChatRelease = 0;
int pause, stop, playing, roomPlaying, needReconnect, readingRtmp,
		chatreadingRtmp;
// 默认是1，否则
int isCDN = 1;

// FFMPEG相关变量
ANativeWindow* window;
AVFormatContext *formatCtx = NULL;
int videoStream, audioStream;
AVCodecContext *vCodecCtx, *aCodecCtx;
AVFrame *videoFrame = NULL, *frameRGBA = NULL, *audioFrame = NULL;
struct SwsContext *v_sws_ctx = NULL, *a_sws_ctx = NULL;
unsigned char * iobuffer, *chatBuffer;
void *videoBuf;
int surfaceWidth, surfaceHeight;

// 声音相关
char* audioBuf, *tmpAudioBuf;
int tmpBufOffset = 0, audioBufSize = 0;
int fromOffset = 0, playRound = 0;
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

// char*转化为jstring
jstring sToJstring(JNIEnv* env, const char* pat) {
	return (*env)->NewStringUTF(env, pat);
}

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

void createEngine() {
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

void createBufferQueueAudioPlayer(int rate, int channel, int fmt) {
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

//int RTMP_Call(RTMP *r, char *method, AMFObjectProperty **data, int num) {
//	RTMPPacket packet;
//	char pbuf[1024], *pend = pbuf + sizeof(pbuf);
//	char *enc;
//	packet.m_nChannel = 0x03; /* control channel (invoke) */
//	packet.m_headerType = RTMP_PACKET_SIZE_MEDIUM;
//	packet.m_packetType = RTMP_PACKET_TYPE_INVOKE;
//	packet.m_nTimeStamp = 0;
//	packet.m_nInfoField2 = 0;
//	packet.m_hasAbsTimestamp = 0;
//	packet.m_body = pbuf + RTMP_MAX_HEADER_SIZE;
//
//	AVal av_method;
//	av_method.av_val = method;
//	av_method.av_len = strlen(method);
//	enc = packet.m_body;
//	enc = AMF_EncodeString(enc, pend, &av_method);
//	enc = AMF_EncodeNumber(enc, pend, ++r->m_numInvokes);
//	*enc++ = AMF_NULL;
//
//	int i = 0;
//	for (; i < num; i++) {
//		AMFObjectProperty *prop = *(data + i);
//		enc = AMFProp_Encode(prop, enc, pend);
//	}
//	packet.m_nBodySize = enc - packet.m_body;
//	LOGI("Last point:...");
//	return RTMP_SendPacket(r, &packet, TRUE);
//}

int read_stream_buf(void *opaque, uint8_t* buf, int buf_size) {
	int read_size = 0;
	if (!stop) {
		readingRtmp = 1;
		read_size = RTMP_Read(_rtmp, (char*) buf, buf_size);
		readingRtmp = 0;
	}
	return read_size;
}

/** 读取消息连接数据 */
int read_chat_stream_buf(uint8_t* buf, int buf_size) {
	int read_size = 0;
	if (!stop) {
		chatreadingRtmp = 1;
		read_size = RTMP_Read(_chatRtmp, (char*) buf, buf_size);
		chatreadingRtmp = 0;
	}
	return read_size;
}

int findStreamDecoder(JNIEnv* pEnv) {
	AVCodec *pCodec = NULL, *pAuCodec = NULL;
	int i;
	AVDictionary *optionsDict = NULL, *auOptionsDict = NULL;
	// Register all formats and codecs
	av_register_all();
	formatCtx = avformat_alloc_context();
//	formatCtx->probesize = 100 *1024;
	formatCtx->max_analyze_duration = 5 * AV_TIME_BASE;
	iobuffer = (unsigned char *) av_malloc(32768);
	AVIOContext *avio = avio_alloc_context(iobuffer, 32768, 0, NULL,
			read_stream_buf, NULL, NULL);
	formatCtx->pb = avio;

	// Open video Input
	playing = 1;
	LOGI("Open video Input....");
	if (avformat_open_input(&formatCtx, "", NULL, NULL) != 0) {
		playing = 0;
		return -1; // Couldn't open file
	}
	LOGI("what the fuck....");
//	 Retrieve stream information
	if (avformat_find_stream_info(formatCtx, NULL) < 0) {
		playing = 0;
		return -1; // Couldn't find stream information
	}
	LOGI("what the fuck....");
	(*jvm)->AttachCurrentThread(jvm, &pEnv, NULL);
	LOGI("AttachCurrentThread success");
	jmethodID mid = (*pEnv)->GetStaticMethodID(pEnv, mJClass, "videoLoaded",
			"()V");
	LOGI("find the videoLoaded method");
	(*pEnv)->CallStaticVoidMethod(pEnv, mJClass, mid);
	(*jvm)->DetachCurrentThread(jvm);
	LOGI("Notify Loaded success");

	// Dump information about file onto standard error
	av_dump_format(formatCtx, -1, "", 0);
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
	if (videoStream == -1 || audioStream == -1) {
		playing = 0;
		return -1; // Didn't find a video stream
	}
	LOGI("Found video：%d audio:%d", videoStream, audioStream);
	// Get a pointer to the codec context for the video stream
	vCodecCtx = formatCtx->streams[videoStream]->codec;
	aCodecCtx = formatCtx->streams[audioStream]->codec;
	// Find the decoder for the video stream
	pCodec = avcodec_find_decoder(vCodecCtx->codec_id);
	if (pCodec == NULL) {
		LOGE("Unsupported video codec!\n");
		playing = 0;
		return -1; // Codec not found
	}
	pAuCodec = avcodec_find_decoder(aCodecCtx->codec_id);
	if (pAuCodec == NULL) {
		LOGE("Unsupported Audio codec!\n");
		playing = 0;
		return -1; // Codec not found
	}
	// Open codec
	if (avcodec_open2(vCodecCtx, pCodec, &optionsDict) < 0) {
		playing = 0;
		return -1; // Could not open codec
	}
	if (avcodec_open2(aCodecCtx, pAuCodec, &auOptionsDict) < 0) {
		playing = 0;
		return -1; // Could not open codec
	}
	// Allocate video frame
	videoFrame = avcodec_alloc_frame();
	// Allocate an AVFrame structure
	frameRGBA = avcodec_alloc_frame();
	// Allocate audio frame
	audioFrame = avcodec_alloc_frame();
	LOGI("Alloc frames finished");
	LOGI("towidth=%d toheight=%d", surfaceWidth, surfaceHeight);
	LOGI(
			"fromwidth=%d fromheight=%d pix_fmt=%d", vCodecCtx->width, vCodecCtx->height, vCodecCtx->pix_fmt);
	v_sws_ctx = sws_getContext(vCodecCtx->width, vCodecCtx->height,
			vCodecCtx->pix_fmt, surfaceWidth, surfaceHeight, AV_PIX_FMT_RGBA,
			SWS_BILINEAR, NULL, NULL, NULL);
	LOGI("Alloc v_sws_ctx finished");
	videoBuf = malloc(surfaceWidth * surfaceHeight * 4);
	avpicture_fill((AVPicture *) frameRGBA, videoBuf, AV_PIX_FMT_RGBA,
			surfaceWidth, surfaceHeight);
	LOGI("findStreamDecoder finished");
	LOGI("createBufferQueueAudioPlayer...........");
	createBufferQueueAudioPlayer(aCodecCtx->sample_rate, aCodecCtx->channels,
			SL_PCMSAMPLEFORMAT_FIXED_16);
	LOGI("createBufferQueueAudioPlayer success");
	return 0;
}

void shutdownAudioPlayer() {
	// destroy buffer queue audio player object, and invalidate all associated interfaces
	if (bqPlayerObject != NULL) {
		(*bqPlayerObject)->Destroy(bqPlayerObject);
		bqPlayerObject = NULL;
		bqPlayerPlay = NULL;
		bqPlayerBufferQueue = NULL;
		bqPlayerEffectSend = NULL;
		bqPlayerMuteSolo = NULL;
		bqPlayerVolume = NULL;
	}

	// destroy file descriptor audio player object, and invalidate all associated interfaces
	if (fdPlayerObject != NULL) {
		(*fdPlayerObject)->Destroy(fdPlayerObject);
		fdPlayerObject = NULL;
		fdPlayerPlay = NULL;
		fdPlayerSeek = NULL;
		fdPlayerMuteSolo = NULL;
		fdPlayerVolume = NULL;
	}

	// destroy output mix object, and invalidate all associated interfaces
	if (outputMixObject != NULL) {
		(*outputMixObject)->Destroy(outputMixObject);
		outputMixObject = NULL;
		outputMixEnvironmentalReverb = NULL;
	}

	// destroy engine object, and invalidate all associated interfaces
	if (engineObject != NULL) {
		(*engineObject)->Destroy(engineObject);
		engineObject = NULL;
		engineEngine = NULL;
	}
}

void decodeAndRender() {
	ANativeWindow_Buffer windowBuffer;
	AVPacket packet;
	int i = 0;
	int frameFinished;
	int lineCnt;
	int waitTime, baseTime, pts;
	int audioRet;
	int called = 0;
	tmpAudioBuf = malloc(65535 * 3);

	while (!stop && av_read_frame(formatCtx, &packet) >= 0) {
//		LOGI("decodeAndRender av_read_frame...");
		// Is this a packet from the video stream?
		if (packet.stream_index == videoStream) {
//			LOGI("decodeAndRender Decoding videoStream...");
			if (pause)
				continue;
			// Decode video frame
			avcodec_decode_video2(vCodecCtx, videoFrame, &frameFinished,
					&packet);
			// Did we get a video frame?
			if (frameFinished && window != NULL) {
				// Convert the image from its native format to RGBA
				sws_scale(v_sws_ctx, (uint8_t *) videoFrame->data,
						videoFrame->linesize, 0, vCodecCtx->height,
						frameRGBA->data, frameRGBA->linesize);
				if (pause || window == NULL)
					continue;
				if (ANativeWindow_lock(window, &windowBuffer, NULL) < 0) {
					LOGE("cannot lock window");
				} else {
					// draw the frame on buffer
					int k = 0;
					for (; k < surfaceHeight; ++k) {
						memcpy(windowBuffer.bits + windowBuffer.stride * k * 4,
								frameRGBA->data[0] + surfaceWidth * k * 4,
								surfaceWidth * 4);
						if (window == NULL)
							break;
					}
					// unlock the window buffer and post it to display
					if (window != NULL)
						ANativeWindow_unlockAndPost(window);
					// count number of frames
					++i;
				}
			}
		} else if (packet.stream_index == audioStream) {
//			LOGI("decodeAndRender Decoding Audio...");
			audioRet = avcodec_decode_audio4(aCodecCtx, audioFrame,
					&frameFinished, &packet);
			if (audioRet > 0 && frameFinished) {
				if (a_sws_ctx == NULL) {
					a_sws_ctx = swr_alloc_set_opts(NULL,
							av_get_default_channel_layout(aCodecCtx->channels),
							AV_SAMPLE_FMT_S16, aCodecCtx->sample_rate,
							av_get_default_channel_layout(aCodecCtx->channels),
							aCodecCtx->sample_fmt, aCodecCtx->sample_rate, 0,
							NULL);
					swr_init(a_sws_ctx);
				}
				if (a_sws_ctx) {
					int data_size = av_samples_get_buffer_size(NULL,
							aCodecCtx->channels, audioFrame->nb_samples,
							AV_SAMPLE_FMT_S16, 1);
					if (audioBuf == NULL) {
						audioBuf = malloc(data_size);
						audioBufSize = data_size;
					} else if (audioBufSize < data_size) {
						audioBuf = realloc(audioBuf, data_size);
						audioBufSize = data_size;
					}
					char* outBuf[2] = { audioBuf, 0 };
					int outsamples = swr_convert(a_sws_ctx, outBuf,
							audioFrame->nb_samples,
							(const uint8_t**) audioFrame->data,
							audioFrame->nb_samples);
					if (outsamples < 0) {
						LOGI("Resample Failed");
						return;
					}
					memcpy(tmpAudioBuf + tmpBufOffset + fromOffset, audioBuf,
							data_size);
					tmpBufOffset += data_size;
					if (tmpBufOffset < 12288) {
						continue;
					}
					(*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue,
							tmpAudioBuf + fromOffset, tmpBufOffset);
					if (playRound < 4) {
						fromOffset += tmpBufOffset;
						playRound++;
					} else {
						playRound = 0;
						fromOffset = 0;
					}
					tmpBufOffset = 0;
				}

			} else
				LOGI("Decode Audio FAILED!!!");
//			usleep(5000);
		}
		av_free_packet(&packet);
//		LOGI("Free Packet Success");
	}
	// 如果不是主动停止播放，或者主播下线，回调客户端重连
	if (!stop) {
		// 判断是否断开连接，如果断开，需要重连

		LOGI("call disConnect!!!");
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass, "disConnect",
				"()V");
		if (mid == 0) {
			LOGI("find disConnect error");
			return;
		}
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid);
		(*jvm)->DetachCurrentThread(jvm);
		LOGI("call disConnect!!! end");

	}
	LOGI("total No. of frames decoded and rendered %d", i);
	shutdownAudioPlayer();
//	if (audioBuf != NULL)
//		free(audioBuf);
//	if (tmpAudioBuf != NULL)
//		free(tmpAudioBuf);
//	finishPlaying();
}

void finishPlaying() {
	playing = 0;
	usleep(100000);
	if (iobuffer != NULL)
		av_free(iobuffer);
	av_free(frameRGBA);
	av_free(videoFrame);
	av_free(audioFrame);
	avcodec_close(vCodecCtx);
	avcodec_close(aCodecCtx);
	avformat_close_input(&formatCtx);

	if (audioBuf != NULL)
		free(audioBuf);
	if (tmpAudioBuf != NULL)
		free(tmpAudioBuf);
	LOGI("Freed All");
}

static void invokeHandler(const char *body, unsigned int nBodySize,
		struct RTMP *_rtmp) {
	char *copyBody = malloc(nBodySize);
	if (!copyBody) {
		LOGI("invokeHandler malloc error");
		return;
	}

	memcpy(copyBody, body, nBodySize);

	AMFObject *obj = malloc(sizeof(AMFObject));
	if (!obj) {
		LOGI("invokeHandler malloc2 error");
		return;
	}
	if (AMF_Decode(obj, body, nBodySize, FALSE) < 0) {
		LOGI("invokeHandler AMF_Decode error");
		return;
	}

	LOGI(AMFObject_PROP_STRING(obj, 0));
	// 初始化房间信息，与有无视频流无关
	if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onInitRoom") == 0) {
		AMFObject *sobj = AMFObject_PROP_OBJECT(obj, 5);
		int i = 0;
		for (; i < sobj->o_num; i++) {
			AMFObject *item = AMFObject_PROP_OBJECT(sobj, i);
			int j = 0;
			char *nickname, *photo, *vip_end, *vip_start;
			int uid, sex, type;
			for (; j < item->o_num; j++) {
				char name[256];
				strncpy(name, item->o_props[j].p_name.av_val,
						item->o_props[j].p_name.av_len);
				name[item->o_props[j].p_name.av_len] = 0;
				if (strcmp(name, "headPic") == 0) {
					photo = AMFObject_PROP_STRING(item, j);
				} else if (strcmp(name, "nickname") == 0) {
					nickname = AMFObject_PROP_STRING(item, j);
				} else if (strcmp(name, "type") == 0) {
					type = AMFObject_PROP_NUMBER(item, j);
				} else if (strcmp(name, "vip_start") == 0) {
					vip_start = AMFObject_PROP_STRING(item, j);
				} else if (strcmp(name, "vip_end") == 0) {
					vip_end = AMFObject_PROP_STRING(item, j);
				} else if (strcmp(name, "sex") == 0) {
					sex = AMFObject_PROP_NUMBER(item, j);
				} else if (strcmp(name, "uid") == 0) {
					uid = AMFObject_PROP_NUMBER(item, j);
				}
			}
			(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
			jmethodID mid =
					(*mEnv)->GetStaticMethodID(mEnv, mJClass, "initRoom",
							"(IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
			if (mid == 0) {
				LOGI("find initRoom error");
				return;
			}

			(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, uid, type, sex,
					sToJstring(mEnv, nickname), sToJstring(mEnv, photo),
					sToJstring(mEnv, vip_start), sToJstring(mEnv, vip_end));
			(*jvm)->DetachCurrentThread(jvm);
		}
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onCheckVideo") == 0) {
		AMFObject *sobj = AMFObject_PROP_OBJECT(obj, 5);
		roomPlaying = AMFObject_PROP_NUMBER(sobj, 0);
		// Call Back Java
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass,
				"onCheckVideo", "(I)V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, roomPlaying);
		(*jvm)->DetachCurrentThread(jvm);
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onLogin") == 0) { // 有用户进入房间
		LOGI("On Login");
		AMFObject *sobj = AMFObject_PROP_OBJECT(obj, 5);
		char *nickname, *photo, *vip_end, *vip_start;
		int type, sex, uid;
		int i = 0;
		for (; i < sobj->o_num; i++) {
			char name[256];
			strncpy(name, sobj->o_props[i].p_name.av_val,
					sobj->o_props[i].p_name.av_len);
			name[sobj->o_props[i].p_name.av_len] = 0;
			if (strcmp(name, "headPic") == 0) {
				photo = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "nickname") == 0) {
				nickname = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "type") == 0) {
				type = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "vip_start") == 0) {
				vip_start = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "vip_end") == 0) {
				vip_end = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "sex") == 0) {
				sex = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "uid") == 0) {
				uid = AMFObject_PROP_NUMBER(sobj, i);
			}
		}

		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid =
				(*mEnv)->GetStaticMethodID(mEnv, mJClass, "addUser",
						"(IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		LOGI("found addUser Method");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, uid, type, sex,
				sToJstring(mEnv, nickname), sToJstring(mEnv, photo),
				sToJstring(mEnv, vip_start), sToJstring(mEnv, vip_end));
		(*jvm)->DetachCurrentThread(jvm);
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onLogout") == 0) { // 有用户退出房间
		AMFObject *sobj = AMFObject_PROP_OBJECT(obj, 5);
		int uid, type;
		int i = 0;
		for (; i < sobj->o_num; i++) {
			char name[256];
			strncpy(name, sobj->o_props[i].p_name.av_val,
					sobj->o_props[i].p_name.av_len);
			name[sobj->o_props[i].p_name.av_len] = 0;
			if (strcmp(name, "uid") == 0) {
				uid = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "type") == 0) {
				type = AMFObject_PROP_NUMBER(sobj, i);
			}
		}
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass, "onLogout",
				"(II)V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, uid, type);
		(*jvm)->DetachCurrentThread(jvm);
//		int uid = AMFObject_PROP_NUMBER(sobj, 0);
//		char* nickname = AMFObject_PROP_STRING(sobj, 2);
//		LOGI("%s LOGGED OUT", nickname);
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onChatMsg") == 0) { //聊天消息
		int errCode = AMFObject_PROP_NUMBER(obj, 3);
		AMFObject *sobj = AMFObject_PROP_OBJECT(obj, 5);
		int i = 0;
		char *fromPhoto, *fromNickname, *msg, *toPhoto, *toNickname;
		int from, to;
		int isPrivate = -100;
		for (; i < sobj->o_num; i++) {
			char name[256];
			strncpy(name, sobj->o_props[i].p_name.av_val,
					sobj->o_props[i].p_name.av_len);
			name[sobj->o_props[i].p_name.av_len] = 0;
			if (strcmp(name, "from") == 0) {
				from = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "fromHeadPic") == 0) {
				fromPhoto = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "fromNickname") == 0) {
				fromNickname = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "msg") == 0) {
				msg = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "private") == 0) {
				isPrivate = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "to") == 0) {
				to = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "toHeadPic") == 0) {
				toPhoto = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "toNickname") == 0) {
				toNickname = AMFObject_PROP_STRING(sobj, i);
			}
		}
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid =
				(*mEnv)->GetStaticMethodID(mEnv, mJClass, "onChatMsg",
						"(IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, errCode, from, to,
				sToJstring(mEnv, fromNickname), sToJstring(mEnv, toNickname),
				sToJstring(mEnv, fromPhoto), sToJstring(mEnv, toPhoto),
				sToJstring(mEnv, msg), isPrivate);
		(*jvm)->DetachCurrentThread(jvm);
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onGiftMsg") == 0) { //礼物消息
		int errCode = AMFObject_PROP_NUMBER(obj, 3);
		AMFObject *sobj = AMFObject_PROP_OBJECT(obj, 5);
		int i = 0;
		char *fromPhoto, *fromNickname, *toPhoto, *toNickname, *giftName,
				*giftImg;
		int from, to, giftCount, giftId;
		for (; i < sobj->o_num; i++) {
			char name[256];
			strncpy(name, sobj->o_props[i].p_name.av_val,
					sobj->o_props[i].p_name.av_len);
			name[sobj->o_props[i].p_name.av_len] = 0;
			if (strcmp(name, "from") == 0) {
				from = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "fromHeadPic") == 0) {
				fromPhoto = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "fromNickname") == 0) {
				fromNickname = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "propId") == 0) {
				giftId = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "count") == 0) {
				giftCount = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "propPic") == 0) {
				giftImg = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "propName") == 0) {
				giftName = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "to") == 0) {
				to = AMFObject_PROP_NUMBER(sobj, i);
			} else if (strcmp(name, "toHeadPic") == 0) {
				toPhoto = AMFObject_PROP_STRING(sobj, i);
			} else if (strcmp(name, "toNickname") == 0) {
				toNickname = AMFObject_PROP_STRING(sobj, i);
			}
		}
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid =
				(*mEnv)->GetStaticMethodID(mEnv, mJClass, "onGift",
						"(IIIIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, errCode, giftId,
				giftCount, from, to, sToJstring(mEnv, fromNickname),
				sToJstring(mEnv, toNickname), sToJstring(mEnv, fromPhoto),
				sToJstring(mEnv, toPhoto), sToJstring(mEnv, giftName),
				sToJstring(mEnv, giftImg));
		(*jvm)->DetachCurrentThread(jvm);
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onPublish") == 0) { //视频发布消息
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass, "onPublish",
				"()V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid);
		(*jvm)->DetachCurrentThread(jvm);

	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onUnpublish") == 0) { //视频退出消息
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass, "onUnpublish",
				"()V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid);
		(*jvm)->DetachCurrentThread(jvm);
		stop = 1;
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "onConnectStatus") == 0) { //视频连接状态(比如流服务出错，或者在手机进入房间，又进入网页同一个房间)
		int errCode = AMFObject_PROP_NUMBER(obj, 3);
		(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
		jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass,
				"onConnectStatus", "(I)V");
		(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid, errCode);
		(*jvm)->DetachCurrentThread(jvm);
		stop = 1;
	} else if (AMFObject_PROP_TYPE(obj, 0) == AMF_STRING
			&& strcmp(AMFObject_PROP_STRING(obj, 0), "_result") == 0) { //视频流的回调
		LOGE("_result start 2222 %d", obj->o_num);
		if (obj->o_num > 3) {

			AMFObjectProperty prop = obj->o_props[3];
			if (AMFProp_NAME_LEN(prop) > 255) {
				return;
			}
			char *statusCode;
			int i = 3;
			char name[256];
			strncpy(name, obj->o_props[i].p_name.av_val,
					obj->o_props[i].p_name.av_len);
			LOGE("_result start 3333");
			name[obj->o_props[i].p_name.av_len] = 0;
			if (strcmp(name, "code") == 0) {
				LOGE("_result start 44444");
				statusCode = AMFProp_STRING(prop);
				(*jvm)->AttachCurrentThread(jvm, &mEnv, NULL);
				jmethodID mid = (*mEnv)->GetStaticMethodID(mEnv, mJClass,
						"connectStatus", "(Ljava/lang/String;)V");
				(*mEnv)->CallStaticVoidMethod(mEnv, mJClass, mid,
						sToJstring(mEnv, statusCode));
				(*jvm)->DetachCurrentThread(jvm);
			}
			LOGE("_result start 5555");
		}

	}
	AMF_Reset(obj);
	free(copyBody);
}

void freeAll() {
	free(rtmpUrl);
}

int initRtmp(JNIEnv *pEnv) {
	_rtmp = RTMP_Alloc();
	LOGI("Rtmp Alloc success1");
	RTMP_Init(_rtmp);
	_rtmp->Link.timeout = 5;
	LOGI("Rtmp Init success1");
	RTMP_SetupInvokeHandler(_rtmp, invokeHandler);
	LOGI("RTMP_SetupInvokeHandler success");
	LOGI("Rtmp URL is %s", rtmpUrl);
	int err = RTMP_SetupURL(_rtmp, rtmpUrl);
	if (err <= 0)
		return -1;
	LOGI("RTMP_SetupURL success");
	err = RTMP_Connect(_rtmp, NULL);
	if (err <= 0)
		return -1;
	LOGI("RTMP_Connect success");
	err = RTMP_ConnectStream(_rtmp, 0);
	if (err <= 0)
		return -1;
	LOGI("RTMP_ConnectStream success");
	return 0;
}

int initChatRtmp(JNIEnv *pEnv) {
	_chatRtmp = RTMP_Alloc();
	LOGI("initChatRtmp Rtmp Alloc success1");
	RTMP_Init(_chatRtmp);
	_chatRtmp->Link.timeout = 5;
	LOGI("initChatRtmp Rtmp Init success1");
	RTMP_SetupInvokeHandler(_chatRtmp, invokeHandler);
	LOGI("initChatRtmp RTMP_SetupInvokeHandler success");
	LOGI("initChatRtmp Rtmp URL is %s", chatRtmpUrl);
	int err = RTMP_SetupURL(_chatRtmp, chatRtmpUrl);
	if (err <= 0)
		return -1;
	LOGI("initChatRtmp RTMP_SetupURL success");
	err = RTMP_Connect(_chatRtmp, NULL);
	if (err <= 0)
		return -1;
	LOGI("initChatRtmp RTMP_Connect success");
//	err = RTMP_ConnectStream(_chatRtmp, 0);
//	if (err <= 0)
//		return -1;
//	LOGI("RTMP_ConnectStream success");
	return 0;
}

void isPlaying() {
	if (isCDN == 1) {
		RTMP_SendCall(_chatRtmp, "checkVideo", NULL, 0);
	} else {
		RTMP_SendCall(_rtmp, "checkVideo", NULL, 0);
	}
	LOGI("RTMP call checkVideo success");
}

void sendGift(JNIEnv *pEnv, jobject pObj, int piTo, int piGiftId, int piCount) {
	AMFObjectProperty *data[3];

	data[0] = malloc(sizeof(AMFObjectProperty));
	data[0]->p_name.av_val = NULL;
	data[0]->p_name.av_len = 0;
	data[0]->p_type = AMF_NUMBER;
	data[0]->p_vu.p_number = piGiftId;

	data[1] = malloc(sizeof(AMFObjectProperty));
	data[1]->p_name.av_val = NULL;
	data[1]->p_name.av_len = 0;
	data[1]->p_type = AMF_NUMBER;
	data[1]->p_vu.p_number = piCount;

	data[2] = malloc(sizeof(AMFObjectProperty));
	data[2]->p_name.av_val = NULL;
	data[2]->p_name.av_len = 0;
	data[2]->p_type = AMF_NUMBER;
	data[2]->p_vu.p_number = piTo;

	LOGI("calling rtmp....");
	if (isCDN == 1) {
		RTMP_SendCall(_chatRtmp, "sendGift", data, 3);
	} else {
		RTMP_SendCall(_rtmp, "sendGift", data, 3);
	}
//	free(data[0]);
//	free(props);
}

void sendMsg(JNIEnv *pEnv, jobject pOjb, jstring psTo, jstring psMsg,
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
	if (isCDN == 1) {
//		RTMP_SendCall(_chatRtmp, "sendMsg", &data);
		RTMP_SendCall(_chatRtmp, "sendMsg", &data, 1);
	} else {
//		RTMP_SendCall(_rtmp, "sendMsg", &data);
		RTMP_SendCall(_chatRtmp, "sendMsg", &data, 1);
	}

}

/** 获取视频流主方法*/
void mainProcess(JNIEnv *pEnv) {
	LOGI("mainProcess main thread start");
	isRelease = 1;
	// 1 初始化Rtmp
	if (initRtmp(pEnv) != 0)
		return;

	int res = findStreamDecoder(pEnv);
	if (res == 0)
		decodeAndRender();
	isRelease = 0;
	LOGI("mainProcess main thread over");
}
/** 获取主方法*/
void chatMainProcess(JNIEnv *pEnv) {
	LOGI("chatMainProcess main thread start");
	isChatRelease = 1;
	// 1 初始化Rtmp
	if (initChatRtmp(pEnv) != 0)
		return;
	chatBuffer = (unsigned char *) av_malloc(32768);
	while (!stop && read_chat_stream_buf(chatBuffer, 32768) >= 0) {
	}
	isChatRelease = 0;
	if (chatBuffer != NULL)
		av_free(chatBuffer);
	LOGI("chatMainProcess main thread over");
}

// 设置Surface
void setSurface(JNIEnv *pEnv, jobject pObj, jobject pSurface, int width,
		int height) {
	LOGI("set surface");
	if (0 != pSurface) {
		// get the native window reference
		window = ANativeWindow_fromSurface(pEnv, pSurface);
		// set format and size of window buffer
		ANativeWindow_setBuffersGeometry(window, 0, 0, WINDOW_FORMAT_RGBA_8888);
		surfaceWidth = width;
		surfaceHeight = height;
	} else {
		// release the native window
		LOGI("Release Surface");
		ANativeWindow_release(window);
		window = NULL;
	}
	LOGI("set surface finished");
}

// 启动主线程
void startMainThread(JNIEnv *pEnv, jobject obj, jstring url, jstring anoUrl) {
	//等待一秒时间
	int i;
	for (i = 0; i < 10; i++) {
		if (isRelease != 0) {
			LOGI("wait a moment start")
			usleep(100 * 1000);
		} else {
			break;
		}
	}
	// 1 初始化成员变量
	isCDN = 0; //非CDN方式
	stop = 0;
	readingRtmp = 0;
	playing = 0;
	roomPlaying = -1;
	pause = 0;
	needReconnect = 0;
	char* _url = (char *) (*pEnv)->GetStringUTFChars(pEnv, url, NULL);
	rtmpUrl = (char*) malloc(256);
	strcpy(rtmpUrl, _url);
	mEnv = pEnv;
	jclass clz = (*pEnv)->FindClass(pEnv, "com/lonzh/lib/LZFFmpeg");
	if (clz == 0) {
		LOGI("find LZFFmpeg class error");
		return;
	}
	// 初始化全局变量，记得释放全局变量（保持NewGlobalRef()/DeleteGlobalRef()成对调用）
	mJClass = (jclass)(*pEnv)->NewGlobalRef(pEnv, clz);

	createEngine();

	// 2 启动线程
	pthread_t mainThread;
	pthread_create(&mainThread, NULL, mainProcess, pEnv);
}

void stopMainThread(JNIEnv *pEnv, jobject obj) {
	stop = 1;
	int times = 0;
	for (; times < 20; times++) {
		if (!readingRtmp)
			break;
	}
	int i;
	for (i = 0; i < 10; i++) {
		if (isRelease != 0) {
			LOGI("wait a moment stop")
			usleep(1000 * 100);
		} else {
			break;
		}
	}
	RTMP_Close(_rtmp);
	// 释放全局变量
	(*pEnv)->DeleteGlobalRef(pEnv, mJClass);
	LOGI("stopMainThread main thread");
}

// 启动主线程,使用CDN方式，开启两条连接：视频流与消息流
void startCdnMainThread(JNIEnv *pEnv, jobject obj, jstring url, jstring anoUrl) {
	LOGE("startCdnMainThread")
	//等待一秒时间
	int i;
	for (i = 0; i < 10; i++) {
		if (isRelease != 0 || isChatRelease != 0) {
			LOGI("wait a moment start")
			usleep(100 * 1000);
		} else {
			break;
		}
	}
	// 1 初始化成员变量
	isCDN = 1; //CDN方式
	stop = 0;
	readingRtmp = 0;
	playing = 0;
	roomPlaying = -1;
	pause = 0;
	needReconnect = 0;
	char* _url = (char *) (*pEnv)->GetStringUTFChars(pEnv, url, NULL);
	char* _chaturl = (char *) (*pEnv)->GetStringUTFChars(pEnv, anoUrl, NULL);
	rtmpUrl = (char*) malloc(256);
	chatRtmpUrl = (char*) malloc(256);
	strcpy(rtmpUrl, _url);
	strcpy(chatRtmpUrl, _chaturl);
	mEnv = pEnv;
	jclass clz = (*pEnv)->FindClass(pEnv, "com/lonzh/lib/LZFFmpeg");
	if (clz == 0) {
		LOGI("find LZFFmpeg class error");
		return;
	}
	// 初始化全局变量，记得释放全局变量（保持NewGlobalRef()/DeleteGlobalRef()成对调用）
	mJClass = (jclass)(*pEnv)->NewGlobalRef(pEnv, clz);

	createEngine();
	// 2 启动线程1,读取视频流
//	pthread_t mainThread;
//	pthread_create(&mainThread, NULL, mainProcess, pEnv);
	// 2 启动线程2，读取消息流
	pthread_t mainThread2;
	pthread_create(&mainThread2, NULL, chatMainProcess, pEnv);
}

void stopCdnMainThread(JNIEnv *pEnv, jobject obj) {
	stop = 1;
	int times = 0;
	for (; times < 20; times++) {
		if (!readingRtmp)
			break;
	}
	int i;
	for (i = 0; i < 10; i++) {
		if (isRelease != 0 || isChatRelease != 0) {
			LOGI("wait a moment stop")
			usleep(1000 * 100);
		} else {
			break;
		}
	}
	RTMP_Close(_rtmp);
	RTMP_Close(_chatRtmp);
	// 释放全局变量
	(*pEnv)->DeleteGlobalRef(pEnv, mJClass);
	LOGI("stopCdnMainThread main thread");
}

void pausePlaying() {
	pause = 1;
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	jvm = pVm;
	JNIEnv * env;
	if ((*pVm)->GetEnv(pVm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}
	JNINativeMethod nm[8];
	nm[0].name = "startMainThread";
	nm[0].signature = "(Ljava/lang/String;Ljava/lang/String;)V";
	nm[0].fnPtr = (void*) startMainThread;

	nm[1].name = "stopMainThread";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*) stopMainThread;

	nm[2].name = "setSurface";
	nm[2].signature = "(Landroid/view/Surface;II)V";
	nm[2].fnPtr = (void*) setSurface;

	nm[3].name = "pausePlaying";
	nm[3].signature = "()V";
	nm[3].fnPtr = (void*) pausePlaying;

	nm[4].name = "sendMsg";
	nm[4].signature = "(Ljava/lang/String;Ljava/lang/String;Z)V";
	nm[4].fnPtr = (void*) sendMsg;

	nm[5].name = "sendGift";
	nm[5].signature = "(III)V";
	nm[5].fnPtr = (void*) sendGift;

	nm[6].name = "startCdnMainThread";
	nm[6].signature = "(Ljava/lang/String;Ljava/lang/String;)V";
	nm[6].fnPtr = (void*) startCdnMainThread;

	nm[7].name = "stopCdnMainThread";
	nm[7].signature = "()V";
	nm[7].fnPtr = (void*) stopCdnMainThread;

	jclass ffmpegClass = (*env)->FindClass(env, "com/lonzh/lib/LZFFmpeg");
	(*env)->RegisterNatives(env, ffmpegClass, nm, 8);
	return JNI_VERSION_1_6;
}
