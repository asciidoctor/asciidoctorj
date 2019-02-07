package org.asciidoctor.jruby.internal;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyKernel;
import org.jruby.RubyModule;
import org.jruby.RubyNumeric;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.IOException;
import java.io.OutputStream;

public class RubyOutputStreamWrapper extends RubyObject {

  public static final String RUBY_CLASS_NAME = "OutputStreamWrapper";

  private OutputStream out;

  private int bytesWritten = 0;

  public static IRubyObject wrap(final Ruby rubyRuntime, final OutputStream out) {

    final RubyClass rubyClass = getOrCreateOutputStreamWrapperClass(rubyRuntime);

    final IRubyObject wrapper = rubyClass.allocate();

    ((RubyOutputStreamWrapper) wrapper).setOut(out);

    return wrapper;
  }

  public RubyOutputStreamWrapper(final Ruby rubyRuntime, final RubyClass rubyClass) {
    super(rubyRuntime, rubyClass);
  }

  public void setOut(OutputStream out) {
    this.out = out;
  }

  public OutputStream getOut() {
    return out;
  }

  public static RubyClass getOrCreateOutputStreamWrapperClass(final Ruby rubyRuntime) {
    RubyModule asciidoctorModule = rubyRuntime.getModule("AsciidoctorJ");
    RubyClass outputStreamWrapperClass = asciidoctorModule.getClass(RUBY_CLASS_NAME);
    if (outputStreamWrapperClass != null) {
      return outputStreamWrapperClass;
    }

    final RubyClass rubyClass = asciidoctorModule.defineClassUnder(RUBY_CLASS_NAME, rubyRuntime.getObject(), new ObjectAllocator() {
      @Override
      public IRubyObject allocate(final Ruby runtime, final RubyClass klazz) {
        return new RubyOutputStreamWrapper(runtime, klazz);
      }
    });

    rubyClass.defineAnnotatedMethods(RubyOutputStreamWrapper.class);

    return rubyClass;
  }

  @JRubyMethod(name = "write", required = 1)
  public IRubyObject write(ThreadContext context, IRubyObject arg) throws IOException {
    writeToStream(arg);
    return context.getRuntime().getNil();
  }

  @JRubyMethod(name = "<<", required = 1)
  public IRubyObject append(ThreadContext context, IRubyObject arg) throws IOException {
    writeToStream(arg);
    return this;
  }

  @JRubyMethod(name = "printf", required = 1, rest = true)
  public IRubyObject printf(ThreadContext context, IRubyObject[] args) throws IOException {
    writeToStream(RubyKernel.sprintf(context, null, args));
    return context.getRuntime().getNil();
  }

  @JRubyMethod(name = "size")
  public IRubyObject size(ThreadContext context) throws IOException {
    return context.getRuntime().newFixnum(bytesWritten);
  }

  private void writeToStream(IRubyObject arg) throws IOException {
    final byte[] bytes = convertToBytes(arg);
    out.write(bytes);
    bytesWritten += bytes.length;
  }

  private byte[] convertToBytes(IRubyObject arg) {
    if (arg instanceof RubyString) {
      return ((RubyString) arg).getBytes();
    } else if (arg instanceof RubyNumeric) {
      return arg.asString().getBytes();
    } else {
      throw new IllegalArgumentException("Don't know how to write a " + arg + " " + arg.getClass());
    }
  }


}
