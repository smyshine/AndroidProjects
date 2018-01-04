package com.tutk.IOTC;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import com.xiaoyi.log.AntsLog;

public class SMsgAVIoctrlStatisticReport {
	public int duration;
	public int bitrate;
	public int got;
	public int lost;
	public long bitCount;

	public boolean isByteOrderBig;

	public SMsgAVIoctrlStatisticReport(boolean isByteOrderBig) {
		this.isByteOrderBig = isByteOrderBig;
	}

	byte[] toByte() {
		if (duration != 0) {
			bitrate = (int) (bitCount * 1000 / duration);
		}
		try {
			AntsLog.d("report_count", "duration=" + duration + "," + "bitrate="
					+ bitrate + ",got=" + got + ",lost=" + lost);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(Packet.intToByteArray(duration, isByteOrderBig), 0, 4);
			dos.write(Packet.intToByteArray(bitrate, isByteOrderBig), 0, 4);
			dos.write(Packet.intToByteArray(got, isByteOrderBig), 0, 4);
			dos.write(Packet.intToByteArray(lost, isByteOrderBig), 0, 4);
			baos.close();
			dos.close();
			return baos.toByteArray();
		} catch (Exception e) {

		}
		return null;

	}
}
