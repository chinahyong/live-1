package com.bixin.bixin.danmu.DanmuBase;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Live on 2016/5/12.
 */
public class DanmakuActionManager implements DanmakuActionInter {

	private List<DanmakuChannelInter> channels = new LinkedList<>();
	private Queue<DanmakuEntity> danEntities = new LinkedList<>();

	@Override
	public void addDanmu(DanmakuEntity dan) {
		danEntities.add(dan);
		looperDan();
	}

	@Override
	public void pollDanmu() {
		looperDan();
	}

	public void addChannel(DanmakuChannelInter channel) {
		channel.setDanmakuActionInter(this);
		channels.add(channel);
	}

	public synchronized void looperDan() {
		for (int i = 0; i < channels.size(); i++) {
			if (!channels.get(i).isRunning() && danEntities.size() > 0) {
				DanmakuEntity poll = danEntities.poll();
				channels.get(i).startAnimation(poll);
			}
		}
	}

	public void clear() {
		danEntities.clear();
		for (int i = 0; i < channels.size(); i++) {
			channels.get(i).releaseView();
		}
	}

	public void release() {
		danEntities.clear();
		for (int i = 0; i < channels.size(); i++) {
			channels.get(i).setDanmakuActionInter(null);
			channels.get(i).releaseView();
		}
	}

}
