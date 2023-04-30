#include <iostream>
#include "rclcpp/rclcpp.hpp"
#include "sensor_msgs/msg/image.hpp"
#include "moveit_msgs/msg/robot_trajectory.hpp"

class RealsenseMoveitZenohNode : public rclcpp::Node
{
public:
  RealsenseMoveitZenohNode() : Node("realsense_moveit_zenoh_node")
  {
    image_subscriber_ = this->create_subscription<sensor_msgs::msg::Image>(
        "/realsense/camera/color/image_raw", 10,
        std::bind(&RealsenseMoveitZenohNode::image_callback, this, std::placeholders::_1));

    trajectory_subscriber_ = this->create_subscription<moveit_msgs::msg::RobotTrajectory>(
        "/move_group/display_planned_path", 10,
        std::bind(&RealsenseMoveitZenohNode::trajectory_callback, this, std::placeholders::_1));
  }

private:
  void image_callback(const sensor_msgs::msg::Image::SharedPtr msg)
  {
    std::cout << "Received image: " << msg->header.stamp.sec << "." << msg->header.stamp.nanosec << std::endl;
    // Process the image message here
  }

  void trajectory_callback(const moveit_msgs::msg::RobotTrajectory::SharedPtr msg)
  {
    std::cout << "Received trajectory: " << msg->header.stamp.sec << "." << msg->header.stamp.nanosec << std::endl;
    // Process the trajectory message here
  }

  rclcpp::Subscription<sensor_msgs::msg::Image>::SharedPtr image_subscriber_;
  rclcpp::Subscription<moveit_msgs::msg::RobotTrajectory>::SharedPtr trajectory_subscriber_;
};

int main(int argc, char **argv)
{
  rclcpp::init(argc, argv);
  rclcpp::spin(std::make_shared<RealsenseMoveitZenohNode>());
  rclcpp::shutdown();
  return 0;
}
