package org.springframework.core;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.junit.Test;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class BridgeMethodResolverTest {

    @Data
    public static abstract class BaseEntity {
        private String id;
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    public static class FooEntity extends BaseEntity {
        private String name;
    }

    public static interface A<T> {
        <S extends T> S test(S T);
    }

    public static interface B<T extends BaseEntity> extends A<T>{
        @Override
        <S extends T> S test(S T);
    }

    public static interface C extends B<FooEntity>{
        @Override
        <S extends FooEntity> S test(S T);
    }

    public static class D<T> {
        public <S extends T> S test(S T){ return null;}
    }

    public static class E<T extends BaseEntity> extends D<T>{
        @Override
        public <S extends T> S test(S T){ return null;}
    }

    public static class F extends E<FooEntity>{
        @Override
        public <S extends FooEntity> S test(S T){ return null;}
    }

    @Test
    public void testInterfacesDefaultBridgeMethodResolver() throws Exception{
        testMethods(BridgeMethodResolver::findBridgedMethod, C.class);
    }


    @Test
    public void testClassesDefaultBridgeMethodResolver() throws Exception{
        testMethods(BridgeMethodResolver::findBridgedMethod, F.class);
    }

    protected void testMethods(Function<Method, Method> resolverMethod, Class<?> clazz) throws Exception{
        for(Method method : clazz.getDeclaredMethods()){
            Method bridged = resolverMethod.apply(method);
            Method expected = clazz.getMethod("test", FooEntity.class);
            assertEquals(msg(method, bridged, expected), expected, bridged);
        }
    }

    protected String msg(Method method, Method bridged, Method expected){
        StringBuilder msg = new StringBuilder();
        msg.append("method:   ").append(method).append("\n");
        msg.append("bridged:  ").append(bridged).append("\n");
        msg.append("expected: ").append(expected);
        return msg.toString();

    }

}

