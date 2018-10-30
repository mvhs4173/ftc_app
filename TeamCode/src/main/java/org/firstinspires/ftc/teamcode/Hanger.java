package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

/*
Use of the 3 positions: open, closed and starting point to fit in the box
Sets position of the hanger on up scale of 0 to 1
Sets hanger to starting position (setOrigin)
When moved from origin, code allows hanger to return to the starting position
 */

public class Hanger {
    private Servo clawServo;
    private DcMotor extensionMotor;
    private ToggleButton decreaseValue,
                         increaseValue;
    private DigitalChannel stopExtender;
    private double origin = 0.48;
    private double currentPos;
    private DigitalChannel lowerLim;
    public State state;

    /**
     * @param hookServo servo to control the grasping
     * @param extensionMotor motor to control the extension
     */
    Hanger(Servo hookServo,
           DcMotor extensionMotor,
           DigitalChannel stopExtender,
           DigitalChannel lowerLim) {
        decreaseValue = new ToggleButton();
        increaseValue = new ToggleButton();
        this.clawServo = hookServo;
        this.extensionMotor = extensionMotor;
        this.stopExtender = stopExtender;
        this.lowerLim = lowerLim;
        currentPos = origin;
        state = State.HANGING;
    }

    public void grip() {
        currentPos = 0.48;
        clawServo.setPosition(currentPos); // on scale of 0 to 1
    }

    public void setOrigin(double newOrigin) {
        origin = newOrigin;
    }

    public void returnToOrigin() {
        clawServo.setPosition(origin);
    }

    public void release() {
        currentPos = 0.3;
        clawServo.setPosition(currentPos);
    }

    /**
     * Use this method to find your servo limits
     * @param decrease button to decrease servo position
     * @param increase button to increase servo position
     */
    public void moveServo(boolean decrease, boolean increase) {
        if (decreaseValue.wasJustClicked(decrease)) {
            currentPos += 0.01;
        } else if (increaseValue.wasJustClicked(increase)) {
            currentPos -= 0.01;
        }
        if (currentPos > 1){
            currentPos = 1;
        } else if (currentPos < 0){
            currentPos = 0;
        }
        clawServo.setPosition(currentPos);
    }

    public void extendHook(){
        extensionMotor.setPower(1);
    }

    public void stopHook(){
        extensionMotor.setPower(0);
    }

    public boolean hang(){
        Timer psi = new Timer();
        extendHook();
        if (stopExtender.getState()){
            stopHook();
            grip();
            psi.init(1);
        }
        if (psi.isTimerUp()) {
            state = State.HANGING;
            return retractHook();
        } else return false;
    }

    public boolean drop() {
        Timer pi = new Timer();
        extendHook();
        if (stopExtender.getState()){
            stopHook();
            release();
            pi.init(1);
        }
        if (pi.isTimerUp()) {
            state = State.ONFLOOR;
            return retractHook();
        } else return false;
    }

    private boolean retractHook() {
        extensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        int target = 0;
        int error = extensionMotor.getCurrentPosition() - target;
        extensionMotor.setTargetPosition(target);
        extensionMotor.setPower(0.5*error);
        if ((error == 0) || (lowerLim.getState())){
            extensionMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            return true;
        } else return false;
    }

    public enum State {
        HANGING,
        ONFLOOR,
        DONE
    }

    public boolean[] getState(){
        return new boolean[]{stopExtender.getState(),lowerLim.getState()};
    }
}