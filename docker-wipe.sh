read -p "Start? (y/n) " ans
[[ $ans =~ ^[Yy]$ ]] || { echo "Aborted."; return; }

docker stack rm services
docker stack rm sites
docker rmi -f $(docker images -qa)
docker system prune -af

rm -rf /home/outsideworx/services
rm -rf /home/outsideworx/sites

apt update
apt upgrade -y
htop
