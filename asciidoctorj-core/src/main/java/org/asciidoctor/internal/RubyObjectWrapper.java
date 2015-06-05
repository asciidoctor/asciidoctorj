package org.asciidoctor.internal;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyBoolean;
import org.jruby.RubyFixnum;
import org.jruby.RubyNil;
import org.jruby.RubyNumeric;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.List;

public class RubyObjectWrapper {

    protected Ruby runtime;
    protected IRubyObject rubyNode;

    public RubyObjectWrapper(IRubyObject rubyNode) {
        this.rubyNode = rubyNode;
        this.runtime = rubyNode.getRuntime();
    }

    public IRubyObject getRubyObject() {
        return rubyNode;
    }

    protected Ruby getRuntime() {
        return runtime;
    }

    protected String getString(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);

        if (result instanceof RubyNil) {
            return null;
        } else if (result instanceof RubySymbol) {
            return ((RubySymbol) result).asJavaString();
        } else {
            return ((RubyString) result).asJavaString();
        }
    }

    protected boolean getBoolean(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return false;
        } else {
            return ((RubyBoolean) result).isTrue();
        }
    }

    protected int getInt(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return 0;
        } else {
            return (int) ((RubyNumeric) result).getLongValue();
        }
    }

    protected <T> List<T> getList(String propertyName, Class<T> elementClass, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return null;
        } else {
            List<T> ret = new ArrayList<T>();
            RubyArray array = (RubyArray) result;
            for (int i = 0; i < array.size(); i++) {
                ret.add(RubyUtils.rubyToJava(runtime, array.at(RubyFixnum.newFixnum(runtime, i)), elementClass));
            }
            return ret;
        }
    }


    protected IRubyObject getRubyProperty(String propertyName, Object... args) {
        ThreadContext threadContext = runtime.getThreadService().getCurrentContext();

        IRubyObject result = null;
        if (propertyName.startsWith("@")) {
            if (args != null) {
                throw new IllegalArgumentException("No args allowed for direct field access");
            }
            result = rubyNode.getInstanceVariables().getInstanceVariable(propertyName);
        } else {
            if (args == null) {
                result = rubyNode.callMethod(threadContext, propertyName);
            } else {
                IRubyObject[] rubyArgs = new IRubyObject[args.length];
                for (int i = 0; i < args.length; i++) {
                    rubyArgs[i] = JavaEmbedUtils.javaToRuby(runtime, args[i]);
                }
                result = rubyNode.callMethod(threadContext, propertyName, rubyArgs);
            }
        }
        return result;
    }

    protected Object getProperty(String propertyName, Object... args) {
        return toJava(getRubyProperty(propertyName, args));
    }

    protected Object toJava(IRubyObject rubyObject) {
        return JavaEmbedUtils.rubyToJava(rubyObject);
    }

    protected <T> T toJava(IRubyObject rubyObject, Class<T> targetClass) {
        return (T) JavaEmbedUtils.rubyToJava(runtime, rubyObject, targetClass);
    }


}
