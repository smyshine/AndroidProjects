package com.tnp.model;


/**
 * Created by Chuanlong on 2015/11/12.
 */
public class st_PPPP_NetInfo {
	byte bFlagInternet		=0;
	byte bFlagHostResolved	=0;
	byte bFlagServerHello	=0;
	byte NAT_Type			=0;
	byte[] MyLanIP          =new byte[16];
	byte[] MyWanIP          =new byte[16];
	
	public String getMyLanIP() { return st_PPPP_Session.bytes2Str(MyLanIP); }
	public String getMyWanIP() { return st_PPPP_Session.bytes2Str(MyWanIP); }	
}
