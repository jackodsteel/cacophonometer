package nz.org.cacophony.birdmonitor;

@FunctionalInterface
public interface StringToEnumConverter<T> {

    T convert(String messageTypeStr);

}
