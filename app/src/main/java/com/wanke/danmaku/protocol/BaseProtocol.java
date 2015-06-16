package com.wanke.danmaku.protocol;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

public abstract class BaseProtocol {
    protected final static String TAG = "protocol";

    protected final short VERSION1 = 0x10;
    protected final short VERSION2 = 0x20;
    protected final BigInteger maxUint8Value = new BigInteger("ffffffffffffffff",
            16);
    protected final BigInteger minUint8Value = new BigInteger("0", 16);
    protected final long maxUint4Value = 0xffffffffl;
    protected final long minUint4Value = 0l;
    protected final int maxUint2Value = 0xffff;
    protected final int minUint2Value = 0;
    protected final short maxUint1Value = 0xff;
    protected final short minUint1Value = 0;

    private static final int crc_table[] = {
            0x0000, 0x1189, 0x2312, 0x329B, 0x4624, 0x57AD, 0x6536, 0x74BF,
            0x8C48, 0x9DC1, 0xAF5A, 0xBED3, 0xCA6C, 0xDBE5, 0xE97E, 0xF8F7,
            0x1081, 0x0108, 0x3393, 0x221A, 0x56A5, 0x472C, 0x75B7, 0x643E,
            0x9CC9, 0x8D40, 0xBFDB, 0xAE52, 0xDAED, 0xCB64, 0xF9FF, 0xE876,
            0x2102, 0x308B, 0x0210, 0x1399, 0x6726, 0x76AF, 0x4434, 0x55BD,
            0xAD4A, 0xBCC3, 0x8E58, 0x9FD1, 0xEB6E, 0xFAE7, 0xC87C, 0xD9F5,
            0x3183, 0x200A, 0x1291, 0x0318, 0x77A7, 0x662E, 0x54B5, 0x453C,
            0xBDCB, 0xAC42, 0x9ED9, 0x8F50, 0xFBEF, 0xEA66, 0xD8FD, 0xC974,
            0x4204, 0x538D, 0x6116, 0x709F, 0x0420, 0x15A9, 0x2732, 0x36BB,
            0xCE4C, 0xDFC5, 0xED5E, 0xFCD7, 0x8868, 0x99E1, 0xAB7A, 0xBAF3,
            0x5285, 0x430C, 0x7197, 0x601E, 0x14A1, 0x0528, 0x37B3, 0x263A,
            0xDECD, 0xCF44, 0xFDDF, 0xEC56, 0x98E9, 0x8960, 0xBBFB, 0xAA72,
            0x6306, 0x728F, 0x4014, 0x519D, 0x2522, 0x34AB, 0x0630, 0x17B9,
            0xEF4E, 0xFEC7, 0xCC5C, 0xDDD5, 0xA96A, 0xB8E3, 0x8A78, 0x9BF1,
            0x7387, 0x620E, 0x5095, 0x411C, 0x35A3, 0x242A, 0x16B1, 0x0738,
            0xFFCF, 0xEE46, 0xDCDD, 0xCD54, 0xB9EB, 0xA862, 0x9AF9, 0x8B70,
            0x8408, 0x9581, 0xA71A, 0xB693, 0xC22C, 0xD3A5, 0xE13E, 0xF0B7,
            0x0840, 0x19C9, 0x2B52, 0x3ADB, 0x4E64, 0x5FED, 0x6D76, 0x7CFF,
            0x9489, 0x8500, 0xB79B, 0xA612, 0xD2AD, 0xC324, 0xF1BF, 0xE036,
            0x18C1, 0x0948, 0x3BD3, 0x2A5A, 0x5EE5, 0x4F6C, 0x7DF7, 0x6C7E,
            0xA50A, 0xB483, 0x8618, 0x9791, 0xE32E, 0xF2A7, 0xC03C, 0xD1B5,
            0x2942, 0x38CB, 0x0A50, 0x1BD9, 0x6F66, 0x7EEF, 0x4C74, 0x5DFD,
            0xB58B, 0xA402, 0x9699, 0x8710, 0xF3AF, 0xE226, 0xD0BD, 0xC134,
            0x39C3, 0x284A, 0x1AD1, 0x0B58, 0x7FE7, 0x6E6E, 0x5CF5, 0x4D7C,
            0xC60C, 0xD785, 0xE51E, 0xF497, 0x8028, 0x91A1, 0xA33A, 0xB2B3,
            0x4A44, 0x5BCD, 0x6956, 0x78DF, 0x0C60, 0x1DE9, 0x2F72, 0x3EFB,
            0xD68D, 0xC704, 0xF59F, 0xE416, 0x90A9, 0x8120, 0xB3BB, 0xA232,
            0x5AC5, 0x4B4C, 0x79D7, 0x685E, 0x1CE1, 0x0D68, 0x3FF3, 0x2E7A,
            0xE70E, 0xF687, 0xC41C, 0xD595, 0xA12A, 0xB0A3, 0x8238, 0x93B1,
            0x6B46, 0x7ACF, 0x4854, 0x59DD, 0x2D62, 0x3CEB, 0x0E70, 0x1FF9,
            0xF78F, 0xE606, 0xD49D, 0xC514, 0xB1AB, 0xA022, 0x92B9, 0x8330,
            0x7BC7, 0x6A4E, 0x58D5, 0x495C, 0x3DE3, 0x2C6A, 0x1EF1, 0x0F78
    };

    private static int COMPUTE(int var, short ch) {
        var = (crc_table[(var ^ ch) & 0xFF] ^ (var >> 8));
        return var;
    }

    public static int crc16(byte[] data, int len) {
        int result = 0;
        short ch;
        for (int i = 0; i < len; ++i)
        {
            ch = (short) (data[i] & 0xff);
            result = COMPUTE(result, ch);
        }
        return result;
    }

    protected static Logger log = LoggerFactory.getLogger("erating_log");
    public static byte end = '\0';
    public static final String CHARSET = "UTF-8";

    protected static byte[] copyOfRange(byte[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        byte[] result = new byte[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    protected static byte[] fixLength(String src, int len) {
        byte[] tmp = null;
        try {
            tmp = src.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {

        }

        if (tmp.length >= len) {
            return copyOfRange(tmp, 0, len);
        } else if (tmp.length < len) {
            int count = len - tmp.length;
            byte[] tmp2 = new byte[len];
            System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
            for (int i = 0; i < count; i++) {
                tmp2[i + tmp.length] = 0x0;
            }

            return tmp2;
        } else {
            return tmp;
        }
    }

    protected static String getStringByLength(IoBuffer buffer, int length) {
        byte[] bs = new byte[length];
        int j = 0;
        boolean findEnd = false;
        for (int i = 0; i < length; i++) {
            byte data = buffer.get();
            if (!findEnd) {
                if (data != end) {
                    bs[j] = data;
                    j++;
                } else {
                    findEnd = true;
                }
            }
        }

        String ret = "";
        try {
            ret = new String(copyOfRange(bs, 0, j), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("", e);
        } finally {
            bs = null;
        }
        return ret;
    }

    private static Integer sequenceId = 1;

    /**
     * 生成socket包的sequence id
     * 
     * @return
     */
    protected int getSequenceId() {
        synchronized (sequenceId) {
            return sequenceId++;
        }
    }

    /**
     * 需要子类实现，用于获取协议command id
     * 
     * @return
     */
    protected abstract int getCommandId();

    /**
     * 获取消息体
     * 
     * @return
     */
    protected abstract byte[] getBody();

    /**
     * 获取消息体的长度
     * 
     * @return
     */
    protected abstract int getBodyLength();

    /**
     * 解析接收到的消息体
     * 
     * @param reqBody
     * @return
     */
    protected abstract int analyzeBody(byte[] reqBody);

    /**
     * 弹幕聊天初始化
     */
    protected static final int CMD_INIT = 0x50000001;
    protected static final int CMD_INIT_RES = 0x60000001;

    /**
     * 弹幕心跳包
     */
    protected static final int CMD_HEARTBEAT = 0x50000002;

    /**
     * 弹幕登录
     */
    protected static final int CMD_USER_LOGIN = 0x50001001;
    protected static final int CMD_USER_LOGIN_RES = 0x60001001;

    /**
     * 弹幕登出
     */
    protected static final int CMD_USER_LOGOUT = 0x50001002;
    protected static final int CMD_USER_LOGOUT_RES = 0x60001002;

    /**
     * 弹幕发送聊天
     */
    protected static final int CMD_USER_CHAT = 0x50002001;
    protected static final int CMD_USER_CHAT_RES = 0x60002001;

    /**
     * 收到弹幕聊天
     */
    protected static final int CMD_USER_CHAT_PUSH = 0x50002002;

    /**
     * 头部的长度
     */
    public final static int headLength = 12;

    /**
     * Resv INT2 2 保留字。
     * Checksum UINT2 2 校验位。
     * checksum的长度
     */
    public final static int checkSumLength = 4;

    /**
     * 获取整个协议的数据包
     * 
     * @return
     */
    public byte[] getMessage() {
        byte[] body = getBody();

        IoBuffer head = IoBuffer.allocate(headLength);

        int totalLength = (headLength + getBodyLength() + checkSumLength);
        IoBuffer datas = IoBuffer.allocate(totalLength);

        // 初始化head
        head.putUnsignedShort(totalLength); // 消息的总长度
        head.put((byte) 0x10); // 版本号
        head.put((byte) 0); // 后续还剩余多少后续包
        head.putInt(getCommandId()); // 填写协议类型
        head.putInt(getSequenceId()); // 填写协议包的序列号
        head.flip();

        datas.put(head);
        if (body != null) {
            datas.put(body);
        }
        datas.putShort((short) 0); // 保留字段，目前写为0
        int checksum = crc16(datas.array(), totalLength - 2);
        datas.putUnsignedShort(checksum);// 写入checkSum
        datas.flip();

        return datas.array();
    }

    /**
     * 解析接收到的消息
     * 
     * @param message
     * @return
     */
    public int analyzeMessage(IoBuffer message) {
        int totalLength = headLength + getBodyLength() + checkSumLength;
        if (message.remaining() < totalLength) {
            return -1;
        }

        System.out.println("totalLength=" + message.getUnsignedShort());
        System.out.println(String.format("version=0x%08x", message.get()));
        System.out.println("remainPackages=" + message.get());
        System.out.println(String.format("commandId=0x%08x",
                message.getUnsignedInt()));
        System.out.println("sequenceId=" + message.getUnsignedInt());

        byte[] ResBody = new byte[getBodyLength()];
        message.get(ResBody);

        return analyzeBody(ResBody);
    }

    /*******************************************************************************************/
    private static Hashtable<Integer, Class> PROTOCOL_HANDLERS = new Hashtable<Integer, Class>();
    static {
        PROTOCOL_HANDLERS.put(Integer.valueOf(CMD_INIT_RES),
                InitConnectionResponse.class);
        PROTOCOL_HANDLERS.put(Integer.valueOf(CMD_USER_CHAT_PUSH),
                PushChatResponse.class);
        PROTOCOL_HANDLERS.put(Integer.valueOf(CMD_USER_CHAT_RES),
                SendChatResponse.class);
        PROTOCOL_HANDLERS.put(Integer.valueOf(CMD_USER_LOGIN_RES),
                LoginResponse.class);
        PROTOCOL_HANDLERS.put(Integer.valueOf(CMD_USER_LOGOUT_RES),
                LogoutResponse.class);
    }

    /**
     * 处理收到的socket消息
     * 
     * @param cmdType
     *            该消息的协议类型
     * @param buffer
     *            该消息的buffer字段
     * @return
     *         该消息是否被处理
     */
    public static boolean handleMessage(int cmdType, byte[] buffer) {
        boolean result = true;

        if (!PROTOCOL_HANDLERS.containsKey(cmdType)) {
            result = false;
        } else {
            IoBuffer message = IoBuffer.allocate(buffer.length);
            message.put(buffer);
            message.flip();

            try {
                BaseProtocol protocolInstance = (BaseProtocol) PROTOCOL_HANDLERS.get(cmdType)
                        .newInstance();
                protocolInstance.analyzeMessage(message);

                handleProtocolListener(protocolInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**************************** 监听接收到协议包 ****************************/
    // 处理协议监听事件
    public static void handleProtocolListener(BaseProtocol baseProtocol) {
        int cmdType = baseProtocol.getCommandId();

        switch (cmdType) {
        case CMD_INIT_RES:
            InitConnectionResponse response = (InitConnectionResponse) baseProtocol;
            Log.d(TAG, "Init Response:" + response.getResult());
            for (ProtocolListener listener : mProtocolListeners) {
                listener.onInitConnectionStatus(response.getResult());
            }
            break;

        case CMD_USER_LOGIN_RES:
            LoginResponse loginResponse = (LoginResponse) baseProtocol;
            Log.d(TAG, "Login Response:" + loginResponse.getResult());
            for (ProtocolListener listener : mProtocolListeners) {
                listener.onLoginStatus(loginResponse.getResult());
            }
            break;

        case CMD_USER_LOGOUT_RES:
            LogoutResponse logoutResponse = (LogoutResponse) baseProtocol;
            Log.d(TAG, "Logout Response:" + logoutResponse.getResult());
            for (ProtocolListener listener : mProtocolListeners) {
                listener.onLogoutStatus(logoutResponse.getResult());
            }
            break;

        case CMD_USER_CHAT_RES:
            SendChatResponse sendChatResponse = (SendChatResponse) baseProtocol;
            Log.d(TAG, "Send Chat Response:" + sendChatResponse.getResult());
            for (ProtocolListener listener : mProtocolListeners) {
                listener.onSendChatStatus(sendChatResponse.getResult());
            }
            break;

        case CMD_USER_CHAT_PUSH:
            PushChatResponse pushChatResponse = (PushChatResponse) baseProtocol;
            Log.d(TAG, "Push Chat Response:" + pushChatResponse.getContent());
            for (ProtocolListener listener : mProtocolListeners) {
                listener.onPushChatReceive(pushChatResponse);
            }
            break;

        default:
            break;
        }

    }

    public static interface ProtocolListener {
        /**
         * 初始化聊天连接的状态
         * 
         * @param status
         *            0表示成功，-1表示失败
         */
        public void onInitConnectionStatus(int status);

        /**
         * 连接聊天服务器的状态
         * 
         * @param status
         *            0表示成功，-1表示失败
         */
        public void onLoginStatus(int status);

        /**
         * 退出聊天服务器的状态
         * 
         * @param status
         *            0表示成功，-1表示失败
         */
        public void onLogoutStatus(int status);

        /**
         * 发送聊天后的结果，目前只有错误的状态
         * 
         * @param status
         *            0表示成功，-1表示失败
         */
        public void onSendChatStatus(int status);

        /**
         * 收到聊天结果
         * 
         * @param pushChatResponse
         */
        public void onPushChatReceive(PushChatResponse pushChatResponse);
    }

    private static HashSet<ProtocolListener> mProtocolListeners = new HashSet<ProtocolListener>();

    public static void addProtocolListener(ProtocolListener listener) {
        synchronized (mProtocolListeners) {
            try {
                mProtocolListeners.add(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeProtocolListener(ProtocolListener listener) {
        synchronized (mProtocolListeners) {
            try {
                mProtocolListeners.remove(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static HashSet<ProtocolListener> getProtocolListeners() {
        synchronized (mProtocolListeners) {
            return mProtocolListeners;
        }
    }
}
