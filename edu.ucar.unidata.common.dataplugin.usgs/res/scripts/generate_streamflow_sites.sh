#!/bin/bash 
declare -a arr=("AL" "AK" "AZ" "AR" "CA" "CT" "DC" "DE" "FL" "GA" 
          "HI" "ID" "IL" "IN" "IA" "KS" "KY" "LA" "ME" "MD" 
          "MA" "MI" "MN" "MS" "MO" "MT" "NE" "NV" "NH" "NJ" 
          "NM" "NY" "NC" "ND" "OH" "OK" "OR" "PA" "PR" "RI" "SC" 
          "SD" "TN" "TX" "UT" "VT" "VA" "WA" "WV" "WI" "WY")
        
for i in "${arr[@]}";do
   wget -O tmp.txt "https://waterservices.usgs.gov/nwis/site/?format=rdb,1.0&stateCd="$i"&parameterCd=00060,00065&siteStatus=active&drainAreaMin=1000"
   cat tmp.txt >> full.txt
   rm -rf tmp.txt
done
wget -O tmp.txt "https://waterservices.usgs.gov/nwis/site/?format=rdb,1.0&stateCd=CO&parameterCd=00060,00065&siteStatus=active&drainAreaMin=250"
cat tmp.txt >> full.txt
rm -rf tmp.txt

##
## CREATE SPI FILE
##

# from StaticPlotInfoPV:
#  REGEX = "^\s*(\d+)\s*(\S+)\s*(-?\d+\.\d+)\s*(-?\d+\.\d+)\s*(-?\d+)\s*(-?\d+\.\d+)\s*(\S*)$"
# example spi file entry:
#  0 02363000 31.59488716 -85.7829975 246 0.00 PEA_RIVER_NEAR_ARITON_AL
#
# BEGINS with a space (^\s*)
# followed by an integer (\d+)
# followed by a space (\s*)
# followed by a non-space characters (\S+)
# followed by a floating-point number with possible negative sign (-?\d+\.\d+)
# followed by a space (\s*)
# followed by an integer with possible negative sign (-?\d+)
# followed by a space (\s*)
# followed by a floating-point number with possible negative sign (-?\d+\.\d+)
# followed by a space (\s*)
# followed by a floating-point number with possible negative sign (-?\d+\.\d+)
# followed by a space (\s*)
# followed by non-space characters at the end of the line (\S*)$

cat full.txt | grep "^USGS"| tr "\\t" ";" | awk -F ";" -v OFS=";" '{print $2,$5,$6,int($9),$1,$3}'| sed -e 's/; /;/' | sed -e 's/ /_/g' | sed -e 's/,//g' | sed -e 's/;USGS;/;0.00;/' > parsed_spi.txt
cat parsed_spi.txt | sed -e 's/^/ 0 /' | sed -e 's/;/ /g' > streamgauge.spi

##
## CREATE SQL SCRIPT
##
# convert from tab to semicolon delimited
cat full.txt | grep "^USGS"| tr "\\t" ";" | awk -F ";" -v OFS=";" '{print $2,$3,$5,$6,$9,$6,$5}' > parsed_sql.txt
# parse to sql query
echo "BEGIN;" > streamflowStations.sql
cat parsed_sql.txt | sed -e 's/^/INSERT INTO "awips"."streamflow_spatial" ("station_id","station_name","source","lat","lon","elev",the_geom) VALUES (/'| sed -e "s/VALUES (/VALUES ('/"| sed -e "s/;/','/"|  sed -e "s/;/','USGS','/"|sed -e "s/;/','/"|sed -e "s/;/','/"| sed -e "s/',' /','/" |sed -e "s/;/',ST_GeometryFromText('POINT(/"| sed -e 's/;/ /' | sed -e "s/$/)',4326));/" | sed -e "s/''/'0'/" >> streamflowStations.sql 
echo "END;" >> streamflowStations.sql
