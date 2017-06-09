package ru.ifmo.ctddev.zyulyaev.compiler.asg.type;

/**
 * @author zyulyaev
 * @since 03.06.2017
 */
public class AsgTypeUtils {
    private AsgTypeUtils() {
    }

    public static int getArrayTypeDepth(AsgType type) {
        if (type instanceof AsgArrayType) {
            return getArrayTypeDepth(((AsgArrayType) type).getCompound()) + 1;
        }
        return 0;
    }

    public static AsgType getCompoundType(AsgType type, int depth) {
        if (depth == 0) {
            return type;
        }
        if (type instanceof AsgArrayType) {
            return getCompoundType(((AsgArrayType) type).getCompound(), depth - 1);
        } else {
            throw new IllegalArgumentException("Cannot get compound type of not array type");
        }
    }
}
