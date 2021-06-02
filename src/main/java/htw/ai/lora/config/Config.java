package htw.ai.lora.config;

import htw.ai.lora.Lora;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : Lora-Protocol
 * @version : 1.0
 * @since : 14-05-2021
 **/
public class Config {
    private String propFileName = "setup.properties";
    Properties prop;
    // Lora Config
    private int carrierFrequency;
    private int power;
    private int modulationBandwidth;
    private int spreadingFactor;
    private int errorCoding;
    private int crc;
    private int implicitHeaderOn;
    private int rxSingleOn;
    private int frequencyHopOn;
    private int hopPeriod;
    private int rxPacketTimeout;
    private int payloadLength;
    private int preambleLength;
    // Lora UART config
    private int baudRate;
    private int parity;
    private int flowControl;
    private int numberOfStopBits;
    private int numberOfDataBits;
    private String port;
    // Lora properties
    private int address;

    /**
     * Read the config file and set properties variables
     *
     * @throws IOException thrown if any I/O error occur
     */
    public void readConfig() throws IOException {
        InputStream inputStream = null;

        try {
            prop = new Properties();
            inputStream = Config.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
            }
            // Get Lora Config
            this.carrierFrequency = Integer.parseInt(prop.getProperty("carrierFrequency"));
            this.power = Integer.parseInt(prop.getProperty("power"));
            this.modulationBandwidth = Integer.parseInt(prop.getProperty("modulationBandwidth"));
            this.spreadingFactor = Integer.parseInt(prop.getProperty("spreadingFactor"));
            this.errorCoding = Integer.parseInt(prop.getProperty("errorCoding"));
            this.crc = Integer.parseInt(prop.getProperty("crc"));
            this.implicitHeaderOn = Integer.parseInt(prop.getProperty("implicitHeaderOn"));
            this.rxSingleOn = Integer.parseInt(prop.getProperty("rxSingleOn"));
            this.frequencyHopOn = Integer.parseInt(prop.getProperty("frequencyHopOn"));
            this.hopPeriod = Integer.parseInt(prop.getProperty("hopPeriod"));
            this.rxPacketTimeout = Integer.parseInt(prop.getProperty("rxPacketTimeout"));
            this.payloadLength = Integer.parseInt(prop.getProperty("payloadLength"));
            this.preambleLength = Integer.parseInt(prop.getProperty("preambleLength"));

            // get the property value
            this.baudRate = Integer.parseInt(prop.getProperty("baudRate"));
            this.parity = Integer.parseInt(prop.getProperty("parity"));
            this.flowControl = Integer.parseInt(prop.getProperty("flowControl"));
            this.numberOfStopBits = Integer.parseInt(prop.getProperty("numberOfStopBits"));
            this.address = Integer.parseInt(prop.getProperty("address"));
            if (address < 0 || address > 9999)
                throw new InvalidPropertiesFormatException("Invalid Address");
            this.port = prop.getProperty("port");
            this.numberOfDataBits = Integer.parseInt(prop.getProperty("numberOfDataBits"));

        } catch (NumberFormatException e) {
            System.out.println("Could not parse String to Integer: " + e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void saveConfig(Config config) throws IOException {
        // Set Lora Config
        prop.setProperty("carrierFrequency", String.valueOf(config.carrierFrequency));
        prop.setProperty("power", String.valueOf(config.power));
        prop.setProperty("modulationBandwidth", String.valueOf(config.modulationBandwidth));
        prop.setProperty("spreadingFactor", String.valueOf(config.spreadingFactor));
        prop.setProperty("errorCoding", String.valueOf(config.errorCoding));
        prop.setProperty("crc", String.valueOf(config.crc));
        prop.setProperty("implicitHeaderOn", String.valueOf(config.implicitHeaderOn));
        prop.setProperty("rxSingleOn", String.valueOf(config.rxSingleOn));
        prop.setProperty("frequencyHopOn", String.valueOf(config.frequencyHopOn));
        prop.setProperty("hopPeriod", String.valueOf(config.hopPeriod));
        prop.setProperty("rxPacketTimeout", String.valueOf(config.rxPacketTimeout));
        prop.setProperty("payloadLength", String.valueOf(config.payloadLength));
        prop.setProperty("preambleLength", String.valueOf(config.preambleLength));

        // Lora UART Config
        prop.setProperty("baudRate", String.valueOf(config.baudRate));
        prop.setProperty("parity", String.valueOf(config.parity));
        prop.setProperty("flowControl", String.valueOf(config.flowControl));
        prop.setProperty("numberOfStopBits", String.valueOf(config.numberOfStopBits));
        prop.setProperty("address", String.valueOf(config.address));
        prop.setProperty("port", String.valueOf(config.port));
        prop.setProperty("numberOfDataBits", String.valueOf(config.numberOfDataBits));

        URL file = Config.class.getClassLoader().getResource(propFileName);
        System.out.println(file.getPath());
        System.out.println(file.getFile());
        prop.store(new FileOutputStream(propFileName), "Configuration File");
    }

    /**
     * Get Parameter Configuration for Lora to configure
     *
     * @return parameter configuration
     */
    public String getConfiguration() {
        final StringBuffer sb = new StringBuffer();
        sb.append(carrierFrequency).append(Lora.DIVIDER.CODE);
        sb.append(power).append(Lora.DIVIDER.CODE);
        sb.append(modulationBandwidth).append(Lora.DIVIDER.CODE);
        sb.append(spreadingFactor).append(Lora.DIVIDER.CODE);
        sb.append(errorCoding).append(Lora.DIVIDER.CODE);
        sb.append(crc).append(Lora.DIVIDER.CODE);
        sb.append(implicitHeaderOn).append(Lora.DIVIDER.CODE);
        sb.append(rxSingleOn).append(Lora.DIVIDER.CODE);
        sb.append(frequencyHopOn).append(Lora.DIVIDER.CODE);
        sb.append(hopPeriod).append(Lora.DIVIDER.CODE);
        sb.append(rxPacketTimeout).append(Lora.DIVIDER.CODE);
        sb.append(payloadLength).append(Lora.DIVIDER.CODE);
        sb.append(preambleLength);
        return sb.toString();
    }

    public String getPropFileName() {
        return propFileName;
    }

    public void setPropFileName(String propFileName) {
        this.propFileName = propFileName;
    }

    public int getCarrierFrequency() {
        return carrierFrequency;
    }

    public void setCarrierFrequency(int carrierFrequency) {
        this.carrierFrequency = carrierFrequency;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getModulationBandwidth() {
        return modulationBandwidth;
    }

    public void setModulationBandwidth(int modulationBandwidth) {
        this.modulationBandwidth = modulationBandwidth;
    }

    public int getSpreadingFactor() {
        return spreadingFactor;
    }

    public void setSpreadingFactor(int spreadingFactor) {
        this.spreadingFactor = spreadingFactor;
    }

    public int getErrorCoding() {
        return errorCoding;
    }

    public void setErrorCoding(int errorCoding) {
        this.errorCoding = errorCoding;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public int getImplicitHeaderOn() {
        return implicitHeaderOn;
    }

    public void setImplicitHeaderOn(int implicitHeaderOn) {
        this.implicitHeaderOn = implicitHeaderOn;
    }

    public int getRxSingleOn() {
        return rxSingleOn;
    }

    public void setRxSingleOn(int rxSingleOn) {
        this.rxSingleOn = rxSingleOn;
    }

    public int getFrequencyHopOn() {
        return frequencyHopOn;
    }

    public void setFrequencyHopOn(int frequencyHopOn) {
        this.frequencyHopOn = frequencyHopOn;
    }

    public int getHopPeriod() {
        return hopPeriod;
    }

    public void setHopPeriod(int hopPeriod) {
        this.hopPeriod = hopPeriod;
    }

    public int getRxPacketTimeout() {
        return rxPacketTimeout;
    }

    public void setRxPacketTimeout(int rxPacketTimeout) {
        this.rxPacketTimeout = rxPacketTimeout;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public int getPreambleLength() {
        return preambleLength;
    }

    public void setPreambleLength(int preambleLength) {
        this.preambleLength = preambleLength;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getFlowControl() {
        return flowControl;
    }

    public void setFlowControl(int flowControl) {
        this.flowControl = flowControl;
    }

    public int getNumberOfStopBits() {
        return numberOfStopBits;
    }

    public void setNumberOfStopBits(int numberOfStopBits) {
        this.numberOfStopBits = numberOfStopBits;
    }

    public int getNumberOfDataBits() {
        return numberOfDataBits;
    }

    public void setNumberOfDataBits(int numberOfDataBits) {
        this.numberOfDataBits = numberOfDataBits;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Properties Filename=").append(propFileName).append(System.lineSeparator());
        sb.append("Carrier Frequency=").append(carrierFrequency).append(System.lineSeparator());
        sb.append("Power=").append(power).append(System.lineSeparator());
        sb.append("Modulation Bandwidth=").append(modulationBandwidth).append(System.lineSeparator());
        sb.append("Spreading Factor=").append(spreadingFactor).append(System.lineSeparator());
        sb.append("Error Coding=").append(errorCoding).append(System.lineSeparator());
        sb.append("CRC=").append(crc).append(System.lineSeparator());
        sb.append("Implicit Header On=").append(implicitHeaderOn).append(System.lineSeparator());
        sb.append("RX Single On=").append(rxSingleOn).append(System.lineSeparator());
        sb.append("Frequency Hop On=").append(frequencyHopOn).append(System.lineSeparator());
        sb.append("Hop Period=").append(hopPeriod).append(System.lineSeparator());
        sb.append("RX Packet Timeout=").append(rxPacketTimeout).append(System.lineSeparator());
        sb.append("Payload Length=").append(payloadLength).append(System.lineSeparator());
        sb.append("Preamble Length=").append(preambleLength).append(System.lineSeparator());
        sb.append("Baud Rate=").append(baudRate).append(System.lineSeparator());
        sb.append("Parity=").append(parity).append(System.lineSeparator());
        sb.append("Flow Control=").append(flowControl).append(System.lineSeparator());
        sb.append("Number Of Stop Bits=").append(numberOfStopBits).append(System.lineSeparator());
        sb.append("Number Of Data Bits=").append(numberOfDataBits).append(System.lineSeparator());
        sb.append("Port=").append(port).append(System.lineSeparator());
        sb.append("ModuleId=").append(address).append(System.lineSeparator());
        return sb.toString();
    }
}
