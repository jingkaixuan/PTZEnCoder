
public class CameraPTZDTO {
	private String cameraNumber = null;
	private String ptzCommand = null;
	private String speed = null;
	private String multiple = null;
	public String getCameraNumber() {
		return cameraNumber;
	}
	public String getPtzCommand() {
		return ptzCommand;
	}
	public String getSpeed() {
		return speed;
	}
	public String getMultiple() {
		return multiple;
	}
	public void setVcnNumber(String vcnNumber) {
		this.cameraNumber = vcnNumber;
	}
	public void setPtzCommand(String ptzCommand) {
		this.ptzCommand = ptzCommand;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public void setMultiple(String multiple) {
		this.multiple = multiple;
	}
	
	
}
