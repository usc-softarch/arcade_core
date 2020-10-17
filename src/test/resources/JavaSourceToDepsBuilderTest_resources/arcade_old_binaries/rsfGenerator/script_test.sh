if [ -z "$1" ];then
	echo "rsfFolder is missing (#1)"
	exit
fi
rsfFolder="$1"

outputFile="file.txt"
errorFile="error.txt"
collectFolder="collect"

rm $collectFolder/*
rm $outputFile
rm $errorFile

for f1 in $rsfFolder/*; do
 	for f2 in $rsfFolder/*; do
 		echo "$f1 $f2"
 		./script_SystemEvo.sh $f1 $f2 r1.txt
 		./script_SystemEvo.sh $f2 $f1 r2.txt
 		r1=$(cat r1.txt)
 		r2=$(cat r2.txt)

 		echo "SysEvo( $f1 , $f2 ) = $r1" >> $outputFile
 		echo "SysEvo( $f2 , $f1 ) = $r2" >> $outputFile
 		

 		echo "r1 : $r1"
 		echo "r2 : $r2"

 		if [ "$r1" != "$r2" ]; then
 			echo "No" >> $outputFile
 			echo "$f1 $f2" >> $errorFile
 			cp $f1 $collectFolder/
 			cp $f2 $collectFolder/
 		else
 			echo "Yes" >> $outputFile
 		fi
	done
done