package com.p2p.pppp_api;

import android.util.Log;

import com.tnp.model.st_PPPP_NetInfo;
import com.tnp.model.st_PPPP_Session;

public class PPPP_APIs {

	public static final int ERROR_PPPP_SUCCESSFUL						=  0;
	
	public static final int ERROR_PPPP_NOT_INITIALIZED					= -3001;
	public static final int ERROR_PPPP_ALREADY_INITIALIZED				= -3002;
	public static final int ERROR_PPPP_TIME_OUT							= -3003;
	public static final int ERROR_PPPP_INVALID_ID						= -3004;
	public static final int ERROR_PPPP_INVALID_PARAMETER				= -3005;
	public static final int ERROR_PPPP_DEVICE_NOT_ONLINE				= -3006;
	public static final int ERROR_PPPP_FAIL_TO_RESOLVE_NAME				= -3007;
	public static final int ERROR_PPPP_INVALID_PREFIX					= -3008;
	public static final int ERROR_PPPP_ID_OUT_OF_DATE					= -3009;
	public static final int ERROR_PPPP_NO_RELAY_SERVER_AVAILABLE		= -3010;
	public static final int ERROR_PPPP_INVALID_SESSION_HANDLE			= -3011;
	public static final int ERROR_PPPP_SESSION_CLOSED_REMOTE			= -3012;
	public static final int ERROR_PPPP_SESSION_CLOSED_TIMEOUT			= -3013;
	public static final int ERROR_PPPP_SESSION_CLOSED_CALLED			= -3014;
	public static final int ERROR_PPPP_REMOTE_SITE_BUFFER_FULL			= -3015;
	public static final int ERROR_PPPP_USER_LISTEN_BREAK				= -3016;
	public static final int ERROR_PPPP_MAX_SESSION						= -3017;
	public static final int ERROR_PPPP_UDP_PORT_BIND_FAILED				= -3018;
	public static final int ERROR_PPPP_USER_CONNECT_BREAK				= -3019;
	public static final int ERROR_PPPP_SESSION_CLOSED_INSUFFICIENT_MEMORY=-3020;
    public static final int ERROR_PPPP_INVALID_APILICENSE				= -3021;
    public static final int ERROR_PPPP_FAIL_TO_CREATE_THREAD			= -3022;
    public static final int ERROR_PPPP_INVALID_SERVER_STRING			= -3023;
    public static final int ERROR_PPPP_SESSION_SOCKET_ERROR             = -3024;
    public static final int ERROR_PPPP_SESSION_DATA_ERROR			    = -3025;
    public static final int ERROR_PPPP_NO_AVAILABLE_P2P_SERVER          = -3026;
	public static final int ERROR_PPPP_TCP_CONNECT_ERROR                = -3027;
	public static final int ERROR_PPPP_TCP_SOCKET_ERROR                 = -3028;
	public static final int ERROR_PPPP_DEVICE_MAX_SESSION               = -3029;
    
    public static final int ERROR_PPPP_NONCE_ERROR                      = -3031;
	public static final int ERROR_PPPP_TICKET_ERROR                     = -3032;
	public static final int ERROR_PPPP_SIGNATURE_ERROR                  = -3033;
	public static final int ERROR_PPPP_KEY_ERROR                        = -3034;
		    
    public static final int ERROR_PPPP_READ_DATA_ERROR                  = -3040;
    public static final int ERROR_PPPP_SEND_DATA_ERROR                  = -3041;

	public static final int ERROR_PPPP_INIT_WINSOCKET_ERROR             = -3050;
    
    public static final int ER_ANDROID_NULL								=-5000;

	
	public native static int PPPP_GetAPIVersion();
	public native static int PPPP_Initialize(byte[] Parameter, int maxSessionNumber);
	public native static int PPPP_DeInitialize();
	public native static int PPPP_NetworkDetect(st_PPPP_NetInfo NetInfo, int UDP_Port);
	public native static int PPPP_NetworkDetectByServer(st_PPPP_NetInfo NetInfo, int UDP_Port, String ServerString);
	public native static int PPPP_Listen_With_Key2(String MyID, int TimeOut_sec, int UDP_Port, byte bEnableInternet,String key, byte bConnectMode);
	public native static int PPPP_Listen_With_Key(String MyID, int TimeOut_sec, int UDP_Port, byte bEnableInternet,String key);
	public native static int PPPP_Listen_Break();
	public native static int PPPP_LoginStatus_Check(byte[] bLoginStatus);
	public native static int PPPP_Connect(String TargetID, byte bEnableLanSearch, int UDP_Port, String Key);
	public native static int PPPP_Check_Buffer(int SessionHandle, byte Channel, int[] WriteSize, int[] ReadSize);
    public native static int PPPP_Probe(String ServerString, int TimeOutSec, byte[] ResultBuf, int BufSize);
	public native static int PPPP_CheckDevOnline(String TargetID,String ServerString, int TimeOut_s,int[] DataBuf);

	public native static int PPPP_ConnectForDoolBell(String TargetID, byte bEnableLanSearch, int UDP_Port, String ServerString, String Key);
	public native static int PPPP_ConnectByServer(String TargetID, byte bEnableLanSearch, int UDP_Port, String ServerString, String Key);
	public native static int PPPP_ConnectByServerDefault(String TargetID, String ServerString, String Key);
	public native static int PPPP_ConnectOnlyLanSearch(String TargetID);
	public native static int PPPP_Connect_To_With_MasterServer(String TargetID, byte bEnableLanSearch, int UDP_Port, String MasterServer, String Key);
	public native static int PPPP_Connect_Break(String TargetID);
	public native static int PPPP_Check(int SessionHandle, st_PPPP_Session SInfo);
	public native static int PPPP_Close(int SessionHandle);
	public native static int PPPP_ForceClose(int SessionHandle);
	public native static int PPPP_Write(int SessionHandle, byte Channel, byte[] DataBuf, int DataSizeToWrite);
	public native static int PPPP_Read(int SessionHandle, byte Channel, byte[] DataBuf, int[] DataSize, int TimeOut_ms);
	public native static int PPPP_Config_Debug(byte bEnableDebug,int DebugLevel);
	public native static int PPPP_Share_Bandwidth(byte bOnOff);
	public native static int PPPP_SendLogin();
	public native static int PPPP_Set_Log_Filename(String Filename);
	public native static int PPPP_IsConnecting();

	public  void onIncomingConnectionCallback()
	{
		System.out.println("onIncomingConnectionCallback");
	}

	static {
		try {
			System.loadLibrary("PPPP_API");
//			ms_verAPI = PPPP_GetAPIVersion();
//			Log.d("PPPP_API","version:"+Integer.toHexString(ms_verAPI));
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary PPPP_API lib," + ule.getMessage());
		}
	}
}
