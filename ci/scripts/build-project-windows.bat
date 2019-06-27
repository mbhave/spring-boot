SET "JAVA_HOME=C:\opt\jdk-8"
SET PATH=%PATH%;C:\Program Files\Git\usr\bin
cd git-repo

echo "./mvnw $*" > build.log
	( ( tail -n 0 -q -f build.log & echo $! >&3 ) 3>pid | grep -v --color=never "\] \[INFO\]\|Building\ jar" | grep --color=never "\[INFO.*---\|Building\ \|SUCCESS\|Total\ time\|Finished\ at\|Final\ Memory" & )
	echo "./mvnw $*"
	./mvnw "$@" >> build.log 2>&1 || (kill "$(<pid)" && sleep 1 && tail -n 3000 build.log && exit 1)
	kill "$(<pid)"
sleep 1