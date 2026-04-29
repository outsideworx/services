read -p "Start? (y/n) " ans
[[ $ans =~ ^[Yy]$ ]] || { echo "Aborted."; return; }

docker stack rm services
docker stack rm sites

echo "Sleep, to make sure everything stopped."
sleep 30

docker secret rm $(docker secret ls -q)
docker rmi -f $(docker images -qa)
docker system prune -af

rm -rf /home/outsideworx/services
rm -rf /home/outsideworx/sites

apt update
apt upgrade -y
htop
