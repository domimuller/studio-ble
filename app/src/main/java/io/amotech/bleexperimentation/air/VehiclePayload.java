package io.amotech.bleexperimentation.air;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.amotech.bleexperimentation.air.stream.bit1.BitInputStream;
import io.amotech.bleexperimentation.air.stream.bit1.BitInputStreamBE;
import io.amotech.bleexperimentation.air.stream.bit1.BitInputStreamLE;
import io.amotech.bleexperimentation.air.stream.bit1.BitOutputStream;
import io.amotech.bleexperimentation.air.stream.bit1.BitOutputStreamBE;
import io.amotech.bleexperimentation.air.stream.bit1.BitOutputStreamLE;

public class VehiclePayload {

    public enum VehicleCategory {
        CATEGORY_TRAM, // 0
        CATEGORY_LIGHT_RAIL, // 1
        CATEGORY_COMMUTER_RAIL, // 2
        CATEGORY_SUBWAY, // 3
        CATEGORY_RAIL, // 4
        CATEGORY_RESERVED_1, // 5
        CATEGORY_RESERVED_2, // 6
        CATEGORY_FUNICULAR, // 7
        CATEGORY_BUS, // 8
        CATEGORY_EXPRESS_BUS, // 9
        CATEGORY_REPLACEMENT_BUS, // 10
        CATEGORY_RESERVED_3, // 11
        CATEGORY_RESERVED_4, // 12
        CATEGORY_CABLE, // 13
        CATEGORY_GONDOLA, // 14
        CATEGORY_FERRY; // 15
    }

    // header
    private int systemId; // uint8

    // vehicle properties
    private int vehicleId; // uint16
    private boolean isAccessible; // bool
    private VehicleCategory category;

    // vehicle signage
    private int lineId;
    private int destStopId;
    private int via1StopId;
    private int via2StopId;
    private int symbolId;

    // vehicle journey
    private boolean isOffCourse;
    private boolean isDiverted;
    private boolean isAtStop;
    private final List<JourneyCall> callList;

    public VehiclePayload() {
        callList = new ArrayList<>();
    }

    public int getSystemId() {
        return systemId;
    }

    public void setSystemId(int systemId) {
        this.systemId = systemId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public int getDestStopId() {
        return destStopId;
    }

    public void setDestStopId(int destStopId) {
        this.destStopId = destStopId;
    }

    public int getVia1StopId() {
        return via1StopId;
    }

    public void setVia1StopId(int via1StopId) {
        this.via1StopId = via1StopId;
    }

    public int getVia2StopId() {
        return via2StopId;
    }

    public void setVia2StopId(int via2StopId) {
        this.via2StopId = via2StopId;
    }

    public int getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(int symbolId) {
        this.symbolId = symbolId;
    }

    public boolean isOffCourse() {
        return isOffCourse;
    }

    public void setOffCourse(boolean offCourse) {
        isOffCourse = offCourse;
    }

    public boolean isDiverted() {
        return isDiverted;
    }

    public void setDiverted(boolean diverted) {
        isDiverted = diverted;
    }

    public boolean isAtStop() {
        return isAtStop;
    }

    public void setAtStop(boolean atStop) {
        isAtStop = atStop;
    }

    public int getCallCount() {
        return callList.size();
    }

    public List<JourneyCall> getCallList() {
        return callList;
    }

    public void addCall(boolean isAccessible, int stopId, int relativeTime) {
        callList.add(new JourneyCall(isAccessible, stopId, relativeTime));
    }

    public void setBytesBE(byte[] bytes) {
        try (BitInputStream stream = BitInputStreamBE.wrapBE(bytes)) {
            parseBytes(stream);
        } catch (IOException e) {
            Log.w("setBytesBE", "something happened", e);
        }
    }

    public void setBytesLE(byte[] bytes) {
        try (BitInputStream stream = BitInputStreamLE.wrapLE(bytes)) {
            parseBytes(stream);
        } catch (IOException e) {
            Log.w("setBytesBE", "something happened", e);
        }
    }

    private void parseBytes(BitInputStream stream) throws IOException {
        systemId = stream.getUnsignedInt(8);
        vehicleId = stream.getUnsignedInt(16);
        stream.getUnsignedInt(3);
        isAccessible = stream.getBoolean();
        category = VehicleCategory.values()[stream.getUnsignedInt(4)];
        lineId = stream.getUnsignedInt(14);
        destStopId = stream.getUnsignedInt(20);
        via1StopId = stream.getUnsignedInt(20);
        via2StopId = stream.getUnsignedInt(20);
        symbolId = stream.getUnsignedInt(6);
        stream.getUnsignedInt(3);
        isOffCourse = stream.getBoolean();
        isDiverted = stream.getBoolean();
        isAtStop = stream.getBoolean();
        int callListSize = stream.getUnsignedInt(2);
        for (int i = 0; i != callListSize; i++) {
            stream.getUnsignedInt(3);
            boolean isAccessible = stream.getBoolean();
            int stopId = stream.getUnsignedInt(20);
            int timeExponent = stream.getUnsignedInt(3);
            int timeMantissa = stream.getUnsignedInt(5);
            callList.add(new JourneyCall(isAccessible, stopId, timeExponent, timeMantissa));
        }
    }


    public byte[] getBytesBE() {
        try (BitOutputStream stream = BitOutputStreamBE.createBE(17 + getCallCount() * 4)) {
            putBytes(stream);
            return stream.array();
        } catch (IOException e) {
            Log.w("getBytesBE", "something happened", e);
            return null;
        }
    }

    public byte[] getBytesLE() {
        try (BitOutputStream stream = BitOutputStreamLE.createLE(17 + getCallCount() * 4)) {
            putBytes(stream);
            return stream.array();
        } catch (IOException e) {
            Log.w("getBytesLE", "something happened", e);
            return null;
        }
    }

    public static VehiclePayload parse(byte[] data) {
        VehiclePayload payload = new VehiclePayload();
        payload.setBytesBE(data);
        return payload;
    }

    private void putBytes(BitOutputStream stream) throws IOException {
        stream.putUnsigned(systemId, 8);
        stream.putUnsigned(vehicleId, 16);
        stream.putUnsigned(0, 3);
        stream.putBoolean(isAccessible);
        stream.putUnsigned(category.ordinal(), 4);
        stream.putUnsigned(lineId, 14);
        stream.putUnsigned(destStopId, 20);
        stream.putUnsigned(via1StopId, 20);
        stream.putUnsigned(via2StopId, 20);
        stream.putUnsigned(symbolId, 6);
        stream.putUnsigned(0, 3);
        stream.putBoolean(isOffCourse);
        stream.putBoolean(isDiverted);
        stream.putBoolean(isAtStop);
        stream.putUnsigned(callList.size(), 2);
        for (JourneyCall call : callList) {
            stream.putUnsigned(0, 3);
            stream.putBoolean(call.isAccessible);
            stream.putUnsigned(call.stopId, 20);
            stream.putUnsigned(call.timeExponent, 3);
            stream.putUnsigned(call.timeMantissa, 5);
        }
    }

    public static class JourneyCall {
        final private boolean isAccessible;
        final private int stopId;
        final private int timeExponent;
        final private int timeMantissa;

        public JourneyCall(boolean isAccessible, int stopId, int relativeTime) {
            this.isAccessible = isAccessible;
            this.stopId = stopId;
            timeExponent = timeExponent(relativeTime);
            timeMantissa = timeMantissa(relativeTime, timeExponent);
        }

        public JourneyCall(boolean isAccessible, int stopId, int timeExponent, int timeMantissa) {
            this.isAccessible = isAccessible;
            this.stopId = stopId;
            this.timeExponent = timeExponent;
            this.timeMantissa = timeMantissa;
        }

        public boolean isAccessible() {
            return isAccessible;
        }

        public int getStopId() {
            return stopId;
        }

        public int getRelativeTime() {
            return relativeTime(timeExponent, timeMantissa);
        }

        private static int timeExponent(int relativeTime) {
            int bucket = (relativeTime + 30) / 30;
            return (int) Math.floor(Math.log(bucket) / Math.log(2));
        }

        private static int timeMantissa(int relativeTime, int timeExponent) {
            double exp2 = Math.pow(2, timeExponent);
            return (int) Math.floor((relativeTime - 30 * (exp2 - 1)) / exp2);
        }

        private static int relativeTime(int timeExponent, int timeMantissa) {
            return (int) Math.pow(2, timeExponent) * (30 + timeMantissa) - 30;
        }

    }

    @NonNull
    @Override
    public String toString() {
        Locale locale = Locale.forLanguageTag("de_CH");
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(locale, "{%n"));
        builder.append(String.format(locale, "  systemId: %d%n", systemId));
        builder.append(String.format(locale, "  vehicleProps: {%n"));
        builder.append(String.format(locale, "    vehicleId: %d%n", vehicleId));
        builder.append(String.format(locale, "    isAccessible: %b%n", isAccessible));
        builder.append(String.format(locale, "    category: %s%n", category));
        builder.append(String.format(locale, "  }%n"));
        builder.append(String.format(locale, "  vehicleSignage: {%n"));
        builder.append(String.format(locale, "    lineId: %d%n", lineId));
        builder.append(String.format(locale, "    destStopId: %d%n", destStopId));
        builder.append(String.format(locale, "    via1StopId: %d%n", via1StopId));
        builder.append(String.format(locale, "    via2StopId: %d%n", via2StopId));
        builder.append(String.format(locale, "    symbolId: %d%n", symbolId));
        builder.append(String.format(locale, "  }%n"));
        builder.append(String.format(locale, "  vehicleJourney: {%n"));
        builder.append(String.format(locale, "    isOffCourse: %b%n", isOffCourse));
        builder.append(String.format(locale, "    isDiverted: %b%n", isDiverted));
        builder.append(String.format(locale, "    isAtStop: %b%n", isAtStop));
        builder.append(String.format(locale, "    calls[%d]: [%n", callList.size()));
        int i = 0;
        for (JourneyCall call : callList) {
            builder.append(String.format(locale, "      call[%d]: {%n", i++));
            builder.append(String.format(locale, "        isAccessible: %b%n", call.isAccessible));
            builder.append(String.format(locale, "        stopId: %d%n", call.stopId));
            builder.append(String.format(locale, "        relativeTime: %d%n", call.getRelativeTime()));
            builder.append(String.format(locale, "      }%n"));
        }
        builder.append(String.format(locale, "    ]%n"));
        builder.append(String.format(locale, "  }%n"));
        builder.append(String.format(locale, "}%n"));
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VehiclePayload payload = (VehiclePayload) obj;
        return toString().equals(payload.toString()); // lazy implementation, very inefficient
    }

    public static VehiclePayload sample1() {
        VehiclePayload sample = new VehiclePayload();
        sample.setSystemId(8);
        sample.setVehicleId(4711);
        sample.setAccessible(true);
        sample.setCategory(VehicleCategory.CATEGORY_BUS);
        sample.setLineId(100);
        sample.setDestStopId(30000);
        sample.setVia1StopId(15000);
        sample.setVia2StopId(25000);
        sample.setSymbolId(7);
        sample.setOffCourse(false);
        sample.setDiverted(false);
        sample.setAtStop(true);
        sample.addCall(true, 11000, 150);
        sample.addCall(true, 12000, 420);
        sample.addCall(true, 13000, 570);
        return sample;
    }

    public static VehiclePayload sample2() {
        VehiclePayload sample = new VehiclePayload();
        sample.setSystemId(8);
        sample.setVehicleId(4712);
        sample.setAccessible(true);
        sample.setCategory(VehicleCategory.CATEGORY_BUS);
        sample.setLineId(100);
        sample.setDestStopId(30000);
        sample.setVia1StopId(25000);
        sample.setSymbolId(7);
        sample.setOffCourse(false);
        sample.setDiverted(true);
        sample.setAtStop(false);
        sample.addCall(true, 16000, 320);
        sample.addCall(true, 17000, 470);
        sample.addCall(true, 18000, 630);
        return sample;
    }

}
