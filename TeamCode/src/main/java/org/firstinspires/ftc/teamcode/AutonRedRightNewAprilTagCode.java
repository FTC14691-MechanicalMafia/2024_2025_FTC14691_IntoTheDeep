package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;


@Autonomous
public class AutonRedRightNewAprilTagCode extends LinearOpMode {
    SpikeCam.location mySpikeLocation;
    private int lookingForTagNumber = 1;
    private AprilTagDetection detectedTag = null;
    CyDogsAprilTags newAprilTags;
    double tagRange = 100;
    double tagBearing = 100;
    double tagYaw = 100;
    double desiredRange = 8.25;
    double timeAprilTagsDriveStarted = 0;
    private CyDogsSparky mySparky;
    private ElapsedTime runtime = new ElapsedTime();
    // This is a SHORT side Auton
    @Override
    public void runOpMode() {

        ElapsedTime runtime = new ElapsedTime();

        telemetry.addLine("Starting Initialization");

        // Set defaults for initialization options
        CyDogsChassis.Direction parkingSpot = CyDogsChassis.Direction.LEFT;

        // Create the instance of sparky, initialize the SpikeCam, devices, and positions
        mySparky = new CyDogsSparky(this, CyDogsChassis.Alliance.RED, 330);
        mySparky.initializeSpikeCam();
        mySparky.initializeDevices();
 //       mySparky.initializePositions();
 //       mySparky.initializeAprilTags();

        newAprilTags = new CyDogsAprilTags(this);


     //   newAprilTags.Initialize();

        // Ask the initialization questions
        parkingSpot = mySparky.askParkingSpot();

        // Wait for the start button to be pressed on the driver station
        waitForStart();


        if (opModeIsActive()) {
            mySparky.initializePositions();
            sleep(300);
            mySpikeLocation = mySparky.spikeCam.getSpikeLocation();

            // Get to standard position before placing purple pixel
            mySparky.MoveStraight(-300, .5, mySparky.StandardAutonWaitTime);
            mySparky.StrafeRight(90,0.5, mySparky.StandardAutonWaitTime);
            mySparky.MoveStraight(-445, .5, mySparky.StandardAutonWaitTime);

            // Place purple pixel and back away from it
            mySparky.AutonPlacePurplePixel(mySpikeLocation);

            if(mySpikeLocation== SpikeCam.location.MIDDLE) {
                mySparky.raiseArmToScore(CyDogsSparky.ArmRaiseBeforeElbowMovement);
                mySparky.MoveStraight(65, .5, mySparky.StandardAutonWaitTime);
            }
            else {
                mySparky.MoveStraight(20, .5, mySparky.StandardAutonWaitTime);
            }

            // do this after purple pixel is placed and spike cam isn't needed anymore
            //newAprilTags.Initialize(mySparky.FrontLeftWheel, mySparky.FrontRightWheel, mySparky.BackLeftWheel, mySparky.FrontRightWheel);


            // First, let's get ourselves straight facing scoring area
            //   Then, adjust position.  Remember dropping purple pixel moved us back from spike 20mm
            if (mySpikeLocation == SpikeCam.location.LEFT) {
                //Already facing the correct way
                //We're 'BackUpDistanceFromSpike' closer to scoreboard
                mySparky.RotateRight(3,.5,mySparky.StandardAutonWaitTime);
                mySparky.StrafeRight(40,.5,mySparky.StandardAutonWaitTime);
                mySparky.raiseArmToScore(CyDogsSparky.ArmRaiseBeforeElbowMovement);
                // has long wait time to handle arm movement before it
                mySparky.MoveStraight(675, .5, 2000);
      // I took 200 off the above to be far enough away to read april tags
            } else if (mySpikeLocation == SpikeCam.location.MIDDLE) {
                mySparky.RotateLeft(92, .5, mySparky.StandardAutonWaitTime);
                // We're 50mm further away from start position
                mySparky.StrafeRight(-50,.5,mySparky.StandardAutonWaitTime);
               // mySparky.raiseArmToScore(CyDogsSparky.ArmRaiseBeforeElbowMovement);
                // has long wait time to handle arm movement before it moves
                mySparky.MoveStraight(500, .5, 2000);
            } else {  //RIGHT
                mySparky.StrafeLeft(CyDogsChassis.OneTileMM, .5, mySparky.StandardAutonWaitTime);
                mySparky.MoveStraight(-CyDogsChassis.OneTileMM-160, .5, mySparky.StandardAutonWaitTime);
                mySparky.StrafeRight(CyDogsChassis.OneTileMM-40, .5, mySparky.StandardAutonWaitTime);
                mySparky.raiseArmToScore(CyDogsSparky.ArmRaiseBeforeElbowMovement);
                // has long wait time to handle arm movement before it
                mySparky.RotateRight(188, .5, 2000);

             }


            // This section gets the robot in front of the april tag
            newAprilTags.Initialize(mySparky.FrontLeftWheel, mySparky.FrontRightWheel, mySparky.BackLeftWheel, mySparky.FrontRightWheel);
            lookingForTagNumber = mySparky.getAprilTagTarget(mySpikeLocation, CyDogsChassis.Alliance.RED);
         //   sleep(300);
        //    ManageDriveToAprilTag();
        //    sleep(100);
            mySparky.ResetWheelConfig();
            sleep(50);
            FinishAprilTagMoves();

            // Finish scoring and park
            mySparky.scoreFromDrivingPositionAndReturn();
            mySparky.MoveStraight(-50,.5,300);
            mySparky.AutonParkInCorrectSpot(mySpikeLocation, parkingSpot);
            mySparky.returnArmFromScoring();
            mySparky.LowerArmAtAutonEnd();
            //mySparky.MoveStraight(100,.5,300);

        }
    }


    private void ManageDriveToAprilTag()
    {
        lookingForTagNumber = mySparky.getAprilTagTarget(mySpikeLocation, CyDogsChassis.Alliance.RED);
        detectedTag = newAprilTags.FindAprilTag(lookingForTagNumber);

        if(detectedTag!=null) {
            timeAprilTagsDriveStarted = runtime.seconds();
            telemetry.addData("Driving to tag!", detectedTag.id);
            tagRange = detectedTag.ftcPose.range;
            tagBearing = detectedTag.ftcPose.bearing;
            tagYaw = detectedTag.ftcPose.yaw;

            // while we're not yet there, keep driving and updating where the tag is
            while (
                    !((desiredRange-.25) <= tagRange && (tagRange <= desiredRange+0.25))
                            || !(-5 <= tagBearing && tagBearing <= 5)
                            || !(-5 <= tagYaw && tagYaw <= 5))
            {
                telemetry.addLine("In the while loop");
                telemetry.addData("during while range:" , tagRange);
                telemetry.addData("during while bearing:" , tagBearing);
                telemetry.addData("during while yaw:" , tagYaw);


                // if we've been going at this for 5 seconds, break out and stop
                if(timeAprilTagsDriveStarted<runtime.seconds()-3){break;}

                // drive to the tag
                newAprilTags.DriveToTag(detectedTag);

                // now that we've driven a fraction of a second, check the tag again
                detectedTag = newAprilTags.FindAprilTag(lookingForTagNumber);

                // if something went wrong and we can't see the tag anymore, give up
                if(detectedTag==null){break;}

                // get new tag positioning
                tagRange = detectedTag.ftcPose.range;
                tagBearing = detectedTag.ftcPose.bearing;
                tagYaw = detectedTag.ftcPose.yaw;
                telemetry.update();
            }

            // sleep(300);
        }

    }

    private void FinishAprilTagMoves()
    {
        // you can use the Yaw from the last time we got the tag, so no need to find it again
        telemetry.addData("Looking for tag:",lookingForTagNumber);
        detectedTag = newAprilTags.FindAprilTag(lookingForTagNumber);

        if(detectedTag!=null) {
            telemetry.addData("during while range:" , tagRange);
            telemetry.addData("during while bearing:" , tagBearing);
            telemetry.addData("during while yaw:" , tagYaw);
            mySparky.RotateLeft((int)detectedTag.ftcPose.yaw,.4, 2000);
        }
        else {
            telemetry.addLine("detected tag is null");
        }

        // after adjusting for Yaw, get the new bearing and adjust for bearing
        detectedTag = newAprilTags.FindAprilTag(lookingForTagNumber);
        if(detectedTag!=null) {
            telemetry.addData("during while range:" , tagRange);
            telemetry.addData("during while bearing:" , tagBearing);
            telemetry.addData("during while yaw:" , tagYaw);
            mySparky.StrafeLeft((int) detectedTag.ftcPose.bearing, .4, 2000);
        }

        // after adjusting for Bearing, get the data again and adjust for range
        detectedTag = newAprilTags.FindAprilTag(lookingForTagNumber);
        if(detectedTag!=null) {
            telemetry.addData("during while range:" , tagRange);
            telemetry.addData("during while bearing:" , tagBearing);
            telemetry.addData("during while yaw:" , tagYaw);
            int moveDistance = (int) (25.4 * (detectedTag.ftcPose.range - desiredRange));
            mySparky.MoveStraight(moveDistance, .4, 2000);
        }
        telemetry.update();

    }


}



