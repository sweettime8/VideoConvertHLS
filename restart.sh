ps ax | grep SK365-GATEWAY | grep -v grep | awk '{print $1}' | xargs kill
nohup java -jar sk365Gateway-0.0.1.jar SK365-GATEWAY &
