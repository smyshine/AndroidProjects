package com.tutk.IOTC;

import java.io.Serializable;
import java.util.UUID;

public class EventInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static byte EVENT_ALL = 0x00, // all event type
			EVENT_MOTIONDECT = 0x01, // motion detect start
			EVENT_VIDEOLOST = 0x02, // video lost alarm
			EVENT_IOALARM = 0x03, // IO alarm start

			EVENT_MOTIONPASS = 0x04, // motion detect end
			EVENT_VIDEORESUME = 0x05, // video resume
			EVENT_IOALARMPASS = 0x06, // IO alarm end

			EVENT_EXPT_REBOOT = 0x10, // system exception reboot
			EVENT_SDFAULT = 0x11; // SDCard record exception

	public static byte STATUS_READED = 1, STATUS_UNREAD = 0;

	public AVIOCTRLDEFs.STimeDay sTimeDay;

	public int length;
	public String uuid = UUID.randomUUID().toString();

	public EventInfo(AVIOCTRLDEFs.STimeDay sTimeDay, int length) {
		this.sTimeDay = sTimeDay;
		this.length = length;
	}

	@Override
	public String toString() {
		return "Event [time=" + sTimeDay.getTimeInMillis() + ", length=" + length + "]";
	}

}
