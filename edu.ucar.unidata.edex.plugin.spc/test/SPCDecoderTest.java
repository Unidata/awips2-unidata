import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.raytheon.uf.common.dataplugin.PluginDataObject;

import edu.ucar.unidata.edex.plugin.spc.SPCDecoder;

public class SPCDecoderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			InputStream stream = SPCDecoderTest.class
					.getResourceAsStream("day2otlk_20180327_0600.kml");
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					stream));
			String input = new String();
			String lineRead;
			lineRead = buf.readLine();
			while ((lineRead = buf.readLine()) != null) {
				// System.out.println("Read in a line "+lineRead);
				input += lineRead + "\n";
			}

			SPCDecoder decoder = new SPCDecoder();
			PluginDataObject[] obj = decoder.decode(input.getBytes());

			//System.out.println(obj[0].getDataTime());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
