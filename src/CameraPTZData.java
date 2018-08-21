
public class CameraPTZData {

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
	
	public static CameraPTZData createFromDTO(CameraPTZDTO dto) {
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
