package org.asciidoctor.internal;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.impl.CursorImpl;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.backtrace.BacktraceElement;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JavaLogger extends RubyObject {


  private static final int DEBUG = 0;
  private static final int INFO = 1;
  private static final int WARN = 2;
  private static final int ERROR = 3;
  private static final int FATAL = 4;
  private static final int UNKNOWN = 5;

  private static final String LOG_PROPERTY_SOURCE_LOCATION = "source_location";
  private static final String LOG_PROPERTY_TEXT = "text";

  static void install(final Ruby runtime) {

    RubyModule asciidoctorModule = runtime.getModule("Asciidoctor");
    final RubyModule loggerManager = asciidoctorModule.defineOrGetModuleUnder("LoggerManager");
    final RubyClass loggerBaseClass = asciidoctorModule.getClass("Logger");

    RubyClass loggerClass = asciidoctorModule
        .defineOrGetModuleUnder("LoggerManager")
        .defineClassUnder("JavaLogger", loggerBaseClass, new ObjectAllocator() {
          @Override
          public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
            return new JavaLogger(runtime, klazz);
          }
        });

    loggerClass.defineAnnotatedMethods(JavaLogger.class);

    IRubyObject logger = loggerClass.allocate();
    logger.callMethod(runtime.getCurrentContext(), "initialize", runtime.getNil());
    loggerManager.callMethod("logger=", logger);

  }

  private JavaLogger(final Ruby runtime, final RubyClass metaClass) {
    super(runtime, metaClass);
  }

  @JRubyMethod(name = "initialize", required = 1, optional = 2)
  public IRubyObject initialize(final ThreadContext threadContext, final IRubyObject[] args) {
    return Helpers.invokeSuper(
        threadContext,
        this,
        getMetaClass(),
        "initialize",
        args,
        Block.NULL_BLOCK);
  }

  /**
   * @param threadContext
   * @param args
   */
  @JRubyMethod(name = "add", required = 1, optional = 2)
  public IRubyObject add(final ThreadContext threadContext, final IRubyObject[] args, Block block) {
    final IRubyObject rubyMessage;
    final String progname;
    if (args.length >= 2 && !args[1].isNil()) {
      rubyMessage = args[1];
      progname = args[2].asJavaString();
    } else if (block.isGiven()) {
      rubyMessage = block.yield(threadContext, getRuntime().getNil());
      progname = this.getInstanceVariable("@progname").asJavaString();
    } else {
      rubyMessage = args[2];
      progname = this.getInstanceVariable("@progname").asJavaString();
    }
    final Cursor cursor = getSourceLocation(rubyMessage);
    final String message = formatMessage(cursor, rubyMessage);
    final Level javaLogLevel = mapRubyLogLevel(args[0]);

    final LogRecord record = createLogRecord(threadContext, javaLogLevel, cursor, message);

    Logger logger = Logger.getLogger(progname);
    logger.log(record);
    return getRuntime().getNil();
  }

  private LogRecord createLogRecord(final ThreadContext threadContext,
                                    final Level logLevel,
                                    final Cursor cursor,
                                    final String message) {
    final LogRecord record = new LogRecord(logLevel, message);
    BacktraceElement[] backtrace = threadContext.getBacktrace();
    record.setSourceClassName(backtrace[2].getFilename());
    record.setSourceMethodName(backtrace[2].getMethod());
    record.setParameters(new Object[] { cursor });
    return record;
  }

  private Level mapRubyLogLevel(IRubyObject arg) {
    final Level javaLevel;
    final int level = arg.convertToInteger().getIntValue();
    switch (level) {
      case DEBUG:
        javaLevel = Level.FINEST;
        break;
      case INFO:
        javaLevel = Level.INFO;
        break;
      case WARN:
        javaLevel = Level.WARNING;
        break;
      case ERROR:
        javaLevel = Level.SEVERE;
        break;
      case FATAL:
        javaLevel = Level.SEVERE;
        break;
      case UNKNOWN:
      default:
        javaLevel = Level.INFO;
        break;
    }
    return javaLevel;
  }

  private String formatMessage(final Cursor cursor, final IRubyObject msg) {
    if (getRuntime().getString().equals(msg.getType())) {
      return msg.asJavaString();
    } else if (getRuntime().getHash().equals(msg.getType())) {
      final RubyHash hash = (RubyHash) msg;
      final String text = Objects.toString(hash.get(getRuntime().newSymbol(LOG_PROPERTY_TEXT)));
      return cursor != null ? cursor.toString() + ": " + text : text;
    }
    throw new IllegalArgumentException(Objects.toString(msg));
  }

  private Cursor getSourceLocation(IRubyObject msg) {
    if (getRuntime().getHash().equals(msg.getType())) {
      final RubyHash hash = (RubyHash) msg;
      final Object sourceLocation = hash.get(getRuntime().newSymbol(LOG_PROPERTY_SOURCE_LOCATION));
      return new CursorImpl((IRubyObject) sourceLocation);
    }
    return null;
  }
}
