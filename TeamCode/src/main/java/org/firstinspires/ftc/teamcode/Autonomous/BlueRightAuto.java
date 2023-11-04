package org.firstinspires.ftc.teamcode.Autonomous;

import static java.lang.Math.toRadians;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robots.BradBot;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequenceBuilder;
@Autonomous(name = "BlueRightPreParkAuto")
public class BlueRightAuto extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        BradBot robot = new BradBot(this, false);
        robot.roadrun.setPoseEstimate(new Pose2d(15.5, 63.25, Math.toRadians(90)));
        int pos = 0;
        TrajectorySequence[] spikePosition = new TrajectorySequence[3];
        spikePosition[0] = robot.roadrun.trajectorySequenceBuilder(new Pose2d(15.5, 63.25, Math.toRadians(-90)))
                .setReversed(true) //spike 1
                .lineToLinearHeading(new Pose2d(23,42,toRadians(0)))
                .addTemporalMarker(robot::done)
                .build();
        spikePosition[1] = robot.roadrun.trajectorySequenceBuilder(new Pose2d(15.5, 63.25, Math.toRadians(-90)))
                .setReversed(true) //spike 2
                .lineToLinearHeading(new Pose2d(15.5, 34.5, toRadians(90)))
                .addTemporalMarker(robot::done)
                .build();
        spikePosition[2] = robot.roadrun.trajectorySequenceBuilder(new Pose2d(15.5, 63.25, Math.toRadians(-90)))
                .setReversed(true) //spike 3
                .splineToLinearHeading(new Pose2d(8, 40, toRadians(45)), toRadians(225))
                .addTemporalMarker(robot::done)
                .build();
        TrajectorySequence[] dropAndPark = new TrajectorySequence[3];
        dropAndPark[0]= robot.roadrun.trajectorySequenceBuilder(spikePosition[0].end())
                .lineToConstantHeading(new Vector2d(23, 47))
                .setReversed(false)
                .lineToLinearHeading(new Pose2d(51, 41.5,toRadians(0)))
                .lineToLinearHeading(new Pose2d(51, 60, toRadians(0)))
                .lineToLinearHeading(new Pose2d(60, 60, toRadians(0)))
                .addTemporalMarker(robot::done)
                .build();
        dropAndPark[1]= robot.roadrun.trajectorySequenceBuilder(spikePosition[1].end())
                .lineToConstantHeading(new Vector2d(15.5, 37))
                .setReversed(false)
                .lineToLinearHeading(new Pose2d(51, 35.5, toRadians(0)))
                .lineToLinearHeading(new Pose2d(51, 60, toRadians(0)))
                .lineToLinearHeading(new Pose2d(60, 60, toRadians(0)))
                .addTemporalMarker(robot::done)
                .build();
        dropAndPark[2] = robot.roadrun.trajectorySequenceBuilder(spikePosition[2].end())
                .lineToConstantHeading(new Vector2d(13, 42))
                .setReversed(false)
                .splineTo(new Vector2d(51, 30.5), toRadians(0))
                .lineToLinearHeading(new Pose2d(51, 60, toRadians(0)))
                .lineToLinearHeading(new Pose2d(60, 60, toRadians(0)))
                .addTemporalMarker(robot::done)
                .build();
        waitForStart();
        while(!isStopRequested()&&opModeIsActive()&&!robot.queuer.isFullfilled()){
            robot.followTrajSeq(spikePosition[pos]);
            robot.queuer.waitForFinish();
            robot.preloadAuto();
            robot.queuer.waitForFinish();
            robot.followTrajSeq(dropAndPark[pos]);
            robot.queuer.setFirstLoop(false);
            robot.update();
        }
    }
}