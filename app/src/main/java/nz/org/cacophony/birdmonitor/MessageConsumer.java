package nz.org.cacophony.birdmonitor;

@FunctionalInterface
public interface MessageConsumer<T> {

    void consume(T messageType, String messageToDisplay);

}
