Testing the SMTP channel locally can be done by starting a local smtp server like e.g. James

`docker run --hostname localhost -p "25:25" -p "110:110" -p "143:143" -p "465:465" -p "587:587" -p "993:993" --name james_run -d linagora/james-jpa-sample:latest`

`docker exec james_run java -jar /root/james-cli.jar AddUser test@localhost test`