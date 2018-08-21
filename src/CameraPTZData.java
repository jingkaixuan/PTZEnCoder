import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CameraPTZData {

	private static List<Integer> ptzOperations = Arrays.asList(0, 1, 2, 3, 4, 5);
	private static List<Integer> fiOperations = Arrays.asList(6, 7, 8, 9);

	private String vcnID = null;
	private String cameraID = null;
	private String ptzCommand = null;

	public String getVcnID() {
		return vcnID;
	}

	public void setVcnID(String vcnID) {
		this.vcnID = vcnID;
	}

	public String getCameraID() {
		return cameraID;
	}

	public void setCameraID(String cameraID) {
		this.cameraID = cameraID;
	}

	public String getPtzCommand() {
		return ptzCommand;
	}

	public void setPtzCommand(String ptzCommand) {
		this.ptzCommand = ptzCommand;
	}

	public static boolean isPTZOperation(int ptzCommand) {
		return CameraPTZData.ptzOperations.contains(ptzCommand);
	}

	public static boolean isFIOperation(int ptzCommand) {
		return CameraPTZData.fiOperations.contains(ptzCommand);
	}

	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	/**
	 * 将一个字节的某一位设置为1
	 * 
	 * @param i 要设置的位，从右到左依次是0~7位
	 * @return
	 */
	public static byte makeByte(int i) {
		return (byte) (0x01 << i);
	}

	public static String hexFormat(int value, boolean uppercase) {
		String ret = String.format("%02x", value).toUpperCase();

		if (uppercase) {
			ret = ret.toUpperCase();
		}
		return ret;
	}

	public static CameraPTZData createFromDTO(CameraPTZDTO dto) {
		if (null == dto) {
			return null;
		}

		String cameraNumber = dto.getCameraNumber();
		if (null == cameraNumber || 40 != cameraNumber.length()) {
			return null;
		}

		// parse VCN ID and VCN ID
		String vcnID = cameraNumber.substring(0, 19);
		String cameraID = cameraNumber.substring(20);

		CameraPTZData ptzData = new CameraPTZData();
		ptzData.setVcnID(vcnID);
		ptzData.setCameraID(cameraID);

		// byte 1-3
		StringBuilder sb = new StringBuilder();
		int bytes[] = new int[8];
		bytes[0] = 0xA5;
		bytes[1] = 0x0F;
		bytes[2] = 0x4D;

		for (int i = 0; i < 3; ++i) {
			sb.append(CameraPTZData.hexFormat(bytes[i], true));
		}

		// encode ptz command
		if (!CameraPTZData.isInteger(dto.getPtzCommand())) {
			return null;
		}
		int ptzCommand = Integer.parseInt(dto.getPtzCommand());
		if (CameraPTZData.isPTZOperation(ptzCommand)) {
			// byte-4
			bytes[3] = CameraPTZData.makeByte(5 - ptzCommand);
			sb.append(String.format("%02x", bytes[3]).toUpperCase());

			// byte 5~7
			if (!CameraPTZData.isInteger(dto.getSpeed())) {
				return null;
			}

			int speed = Integer.parseInt(dto.getSpeed());
			// byte-5
			bytes[4] = 0;
			if (CameraPTZData.isHorizationPTZ(ptzCommand)) {
				bytes[4] = speed;
			}
			sb.append(String.format("%02x", bytes[4]).toUpperCase());

			// byte-6
			bytes[5] = 0;
			if (CameraPTZData.isVerticalPTZ(ptzCommand)) {
				bytes[5] = speed;
			}
			sb.append(String.format("%02x", bytes[5]).toUpperCase());

			// byte-7
			bytes[6] = 0x04;
			if (CameraPTZData.isZoomPTZ(ptzCommand)) {
				bytes[6] |= speed << 4;
			}
			sb.append(String.format("%02x", bytes[6]).toUpperCase());

		} else if (CameraPTZData.isFIOperation(ptzCommand)) {
			// byte-4
			bytes[3] = 0x04 << 4 | CameraPTZData.makeByte(9 - ptzCommand);
			sb.append(CameraPTZData.hexFormat(bytes[3], true));

			// byte 5~7
			if (!CameraPTZData.isInteger(dto.getMultiple())) {
				return null;
			}

			int multiple = Integer.parseInt(dto.getMultiple());

			// byte5
			bytes[4] = 0;
			if (CameraPTZData.isIrisFI(ptzCommand)) {
				bytes[4] = multiple;
			}

			sb.append(CameraPTZData.hexFormat(bytes[4], true));

			// byte6
			bytes[5] = 0;
			if (CameraPTZData.isFocusFI(ptzCommand)) {
				bytes[5] = multiple;
			}

			sb.append(CameraPTZData.hexFormat(bytes[5], true));

			// byte7
			bytes[6] = 0x04;
			sb.append(CameraPTZData.hexFormat(bytes[6], true));
		} else if (CameraPTZData.isStopPTZOperation(ptzCommand)) {
			for (int i = 3; i < 6; ++i) {
				bytes[i] = 0;
				sb.append(CameraPTZData.hexFormat(bytes[i], true));
			}
			bytes[6] = 0x04;
			sb.append(CameraPTZData.hexFormat(bytes[6], true));
		} else if (CameraPTZData.isStopFIOperation(ptzCommand)) {
			bytes[3] = 0x40;
			bytes[4] = 0;
			bytes[5] = 0;
			bytes[6] = 0x04;

			for (int i = 3; i < 7; ++i) {
				sb.append(CameraPTZData.hexFormat(bytes[i], true));
			}
		}

		for (int i = 0; i < 7; ++i) {
			bytes[7] += bytes[i];
		}
		bytes[7] = bytes[7] % 256;
		sb.append(CameraPTZData.hexFormat(bytes[7], true));

		ptzData.setPtzCommand(sb.toString());

		return ptzData;
	}

	private static boolean isStopPTZOperation(int ptzCommand2) {
		return ptzCommand2 == PTZOperation.STOP_PTZ.ordinal();
	}

	private static boolean isStopFIOperation(int ptzCommand) {
		return ptzCommand == PTZOperation.STOP_FI.ordinal();
	}

	private static boolean isFocusFI(int ptzCommand2) {
		return ptzCommand2 == PTZOperation.FI_IN.ordinal() || ptzCommand2 == PTZOperation.FI_OUT.ordinal();
	}

	private static boolean isIrisFI(int ptzCommand2) {
		return ptzCommand2 == PTZOperation.FI_NEAR.ordinal() || ptzCommand2 == PTZOperation.FI_FAR.ordinal();
	}

	private static boolean isHorizationPTZ(int ptzCommand2) {
		// TODO Auto-generated method stub
		return PTZOperation.PTZ_LEFT.ordinal() == ptzCommand2 || PTZOperation.PTZ_RIGHT.ordinal() == ptzCommand2;
	}

	private static boolean isVerticalPTZ(int ptzCommand) {
		return PTZOperation.PTZ_UP.ordinal() == ptzCommand || PTZOperation.PTZ_DOWN.ordinal() == ptzCommand;
	}

	private static boolean isZoomPTZ(int ptzCommand) {
		return PTZOperation.ZOOM_IN.ordinal() == ptzCommand || PTZOperation.ZOOM_OUT.ordinal() == ptzCommand;
	}

	@Override
	public String toString() {
		return "VCN_ID: " + this.vcnID + "\r\n" + "CAMERA_ID: " + this.cameraID + "\r\n" + "PTZCMD: " + this.ptzCommand;
	}

	public static void main(String[] args) {
		{

			CameraPTZDTO dto = new CameraPTZDTO();
			dto.setVcnNumber("1111111111111111111122222222222222222222");
			dto.setPtzCommand("5");
			dto.setSpeed("5");
			dto.setMultiple("");

			CameraPTZData data = CameraPTZData.createFromDTO(dto);

			System.out.println(data);
		}
		
		{

			CameraPTZDTO dto = new CameraPTZDTO();
			dto.setVcnNumber("1111111111111111111122222222222222222222");
			dto.setPtzCommand("11");
			dto.setSpeed("5");
			dto.setMultiple("");

			CameraPTZData data = CameraPTZData.createFromDTO(dto);

			System.out.println(data);
		}
		
		{

			CameraPTZDTO dto = new CameraPTZDTO();
			dto.setVcnNumber("1111111111111111111122222222222222222222");
			dto.setPtzCommand("10");
			dto.setSpeed("5");
			dto.setMultiple("");

			CameraPTZData data = CameraPTZData.createFromDTO(dto);

			System.out.println(data);
		}
	}

}
