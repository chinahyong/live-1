package com.bixin.bixin.emoji;


import java.util.HashMap;
import java.util.Map;

import tv.live.bx.R;

public class MsgFaceUtils {

	public static Map<String, Integer> facesMap = new HashMap<>();

	public static String[] faceImgNames = new String[] { "[大笑]", "[色]", "[失望]", "[脸红]", "[开心]", "[嫌弃]", "[呸]", "[吐舌]",
			"[热情]", "[露齿笑]", "[汗]", "[笑脸]", "[担心]", "[恐惧]", "[低落]", "[生气]", "[哭]", "[破涕为笑]", "[飞吻]", "[悔恨]", "[亲吻]",
			"[鬼脸]", "[眨眼]", "[晕]", "[焦虑]", "[满意]", "[生病]", "[外星人]", "[心]", "[心碎]", "[喝彩]", "[拳头]", "[好的]", "[强]",
			"[弱]", "[红唇]", "[鸡]", "[羊]", "[熊]", "[猪]", "[象]", "[猴]", "[虎]", "[兔]", "[鲸鱼]", "[狗]", "[上]", "[下]", "[右]",
			"[左]", "[药丸]", "[面包]", "[草莓]", "[香蕉]", "[苹果]", "[西红柿]", "[西瓜]", "[冰激凌]", "[丸子]", "[闪电]", "[晴天]", "[月亮]",
			"[日全食]", "[太阳]", "[茶]", "[奶瓶]", "[仙人掌]", "[便便]", "[吹气]", "[非礼勿视]" };

	public static int[] faceImgs = new int[] { R.drawable.emoji_1f603, R.drawable.emoji_1f60d, R.drawable.emoji_1f614,
			R.drawable.emoji_1f633, R.drawable.emoji_1f604, R.drawable.emoji_1f612, R.drawable.emoji_1f616,
			R.drawable.emoji_1f61d, R.drawable.emoji_263a, R.drawable.emoji_1f601, R.drawable.emoji_1f613,
			R.drawable.emoji_1f60a, R.drawable.emoji_1f630, R.drawable.emoji_1f631, R.drawable.emoji_1f61e,
			R.drawable.emoji_1f621, R.drawable.emoji_1f62d, R.drawable.emoji_1f602, R.drawable.emoji_1f618,
			R.drawable.emoji_1f623, R.drawable.emoji_1f61a, R.drawable.emoji_1f61c, R.drawable.emoji_1f609,
			R.drawable.emoji_1f632, R.drawable.emoji_1f625, R.drawable.emoji_1f60c, R.drawable.emoji_1f637,
			R.drawable.emoji_1f47d, R.drawable.emoji_2764, R.drawable.emoji_1f494, R.drawable.emoji_1f44f,
			R.drawable.emoji_1f44a, R.drawable.emoji_1f44c, R.drawable.emoji_1f44d, R.drawable.emoji_1f44e,
			R.drawable.emoji_1f48b, R.drawable.emoji_1f414, R.drawable.emoji_1f40f, R.drawable.emoji_1f43b,
			R.drawable.emoji_1f437, R.drawable.emoji_1f418, R.drawable.emoji_1f412, R.drawable.emoji_1f42f,
			R.drawable.emoji_1f430, R.drawable.emoji_1f433, R.drawable.emoji_1f436, R.drawable.emoji_1f446,
			R.drawable.emoji_1f447, R.drawable.emoji_1f449, R.drawable.emoji_1f448, R.drawable.emoji_1f48a,
			R.drawable.emoji_1f35e, R.drawable.emoji_1f353, R.drawable.emoji_1f34c, R.drawable.emoji_1f34e,
			R.drawable.emoji_1f345, R.drawable.emoji_1f349, R.drawable.emoji_1f366, R.drawable.emoji_1f361,
			R.drawable.emoji_26a1, R.drawable.emoji_2600, R.drawable.emoji_1f319, R.drawable.emoji_1f31a,
			R.drawable.emoji_1f31d, R.drawable.emoji_1f375, R.drawable.emoji_1f37c, R.drawable.emoji_1f335,
			R.drawable.emoji_1f4a9, R.drawable.emoji_1f4a8, R.drawable.emoji_1f648 };

	static {
		for (int i = 0; i < faceImgNames.length; i++)
			facesMap.put(faceImgNames[i], faceImgs[i]);
	}

}
