#!/bin/sh
sudo rabbitmqctl delete_vhost mule-test
sudo rabbitmqctl delete_user mule

sudo rabbitmqctl add_vhost mule-test
sudo rabbitmqctl add_user mule elum
sudo rabbitmqctl set_permissions -p mule-test mule ".*" ".*" ".*"
