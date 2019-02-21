#!/bin/bash 
declare -a arr=("AL" "AK" "AZ" "AR" "CA" "CO" "CT" "DC" "DE" "FL" "GA" 
          "HI" "ID" "IL" "IN" "IA" "KS" "KY" "LA" "ME" "MD" 
          "MA" "MI" "MN" "MS" "MO" "MT" "NE" "NV" "NH" "NJ" 
          "NM" "NY" "NC" "ND" "OH" "OK" "OR" "PA" "PR" "RI" "SC" 
          "SD" "TN" "TX" "UT" "VT" "VA" "WA" "WV" "WI" "WY")
for i in "${arr[@]}";do
   wget -O tmp.txt "https://waterservices.usgs.gov/nwis/site/?format=rdb,1.0&stateCd="$i"&parameterCd=00060,00065&siteStatus=active"
   cat tmp.txt >> full.txt
   rm -rf tmp.txt
done
# convert from tab to semicolon delimited
cat full.txt | grep "^USGS"| tr "\\t" ";" | awk -F ";" -v OFS=";" '{print $2,$3,$5,$6,$9,$6,$5}' > raw.txt
]
# parse to sql query
echo "BEGIN;" > stations.txt 
cat raw.txt | sed -e 's/^/INSERT INTO "awips"."streamflow_spatial" ("station_id","station_name","source","lat","lon","elev",the_geom) VALUES (/'| sed -e "s/VALUES (/VALUES ('/"| sed -e "s/;/','/"|  sed -e "s/;/','USGS','/"|sed -e "s/;/','/"|sed -e "s/;/','/"| sed -e "s/',' /','/" |sed -e "s/;/',ST_GeometryFromText('POINT(/"| sed -e 's/;/ /' | sed -e "s/$/)',4326));/" | sed -e "s/''/'0'/" >> stations.txt 
echo "END;" >> stations.txt 