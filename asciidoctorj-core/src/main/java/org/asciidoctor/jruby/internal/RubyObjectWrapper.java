package org.asciidoctor.jruby.internal;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyFixnum;
import org.jruby.RubyNil;
import org.jruby.RubyNumeric;
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

    public String getString(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);

        if (result instanceof RubyNil) {
            return null;
        } else if (result instanceof RubySymbol) {
            return result.asJavaString();
        } else {
            return result.asJavaString();
        }
    }

    public void setString(String propertyName, String value) {
        if (value == null) {
            setRubyProperty(propertyName, runtime.getNil());
        } else {
            setRubyProperty(propertyName, runtime.newString(value));
        }
    }

    public String getSymbol(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);

        if (result instanceof RubyNil) {
            return null;
        }
        return result.asJavaString();
    }

    public void setSymbol(String propertyName, String value) {
        if (value == null) {
            setRubyProperty(propertyName, runtime.getNil());
        } else {
            setRubyProperty(propertyName, runtime.newSymbol(value));
        }
    }

    public boolean getBoolean(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return false;
        } else {
            return result.isTrue();
        }
    }

    public void setBoolean(String propertyName, boolean value) {
        setRubyProperty(propertyName, runtime.newBoolean(value));
    }

    public int getInt(String propertyName, Object... args) {
        IRubyObject result = getRubyProperty(propertyName, args);
        if (result instanceof RubyNil) {
            return 0;
        } else {
            return (int) ((RubyNumeric) result).getIntValue();
        }
    }

    public void setInt(String propertyName, int value) {
        setRubyProperty(propertyName, runtime.newFixnum(value));
    }


    public <T> List<T> getList(String propertyName, Class<T> elementClass, Object... args) {
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


    public IRubyObject getRubyProperty(String propertyName, Object... args) {
        ThreadContext threadContext = runtime.getThreadService().getCurrentContext();

        IRubyObject result;
        if (propertyName.startsWith("@")) {
            if (args != null && args.length > 0) {
                throw new IllegalArgumentException("No args allowed for direct field access");
            }
            result = rubyNode.getInstanceVariables().getInstanceVariable(propertyName);
        } else {
            if (args == null) {
                result = rubyNode.callMethod(threadContext, propertyName);
            } else {
                IRubyObject[] rubyArgs = new IRubyObject[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof RubyObjectWrapper) {
                        rubyArgs[i] = ((RubyObjectWrapper) args[i]).getRubyObject();
                    } else {
                        rubyArgs[i] = JavaEmbedUtils.javaToRuby(runtime, args[i]);
                    }
                }
                result = rubyNode.callMethod(threadContext, propertyName, rubyArgs);
            }
        }
        return result;
    }

    public void setRubyProperty(String propertyName, IRubyObject arg) {
        ThreadContext threadContext = runtime.getThreadService().getCurrentContext();

        if (propertyName.startsWith("@")) {
            rubyNode.getInstanceVariables().setInstanceVariable(propertyName, arg);
        } else {
            if (arg == null) {
                rubyNode.callMethod(threadContext, propertyName + "=", runtime.getNil());
            } else {
                rubyNode.callMethod(threadContext, propertyName + "=", arg);
            }
        }
    }

    public Object getProperty(String propertyName, Object... args) {
        return toJava(getRubyProperty(propertyName, args));
    }

    public Object toJava(IRubyObject rubyObject) {
        return JavaEmbedUtils.rubyToJava(rubyObject);
    }

    public <T> T toJava(IRubyObject rubyObject, Class<T> targetClass) {
        return (T) JavaEmbedUtils.rubyToJava(runtime, rubyObject, targetClass);
    }

}
