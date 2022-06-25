package ee.ristoseene.rawtex.io.core.common.test;

@FunctionalInterface
public interface LongToByteFunction {
    byte apply(long value);
}
