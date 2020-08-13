package org.asciidoctor.jruby.log.internal;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.ast.Cursor;
import org.asciidoctor.jruby.ast.impl.CursorImpl;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
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
import java.util.Optional;

public class JavaLogger extends RubyObject {

  private final LogHandler rootLogHandler;

  private static final String LOG_PROPERTY_SOURCE_LOCATION = "source_location";
  private static final String LOG_PROPERTY_TEXT = "text";

  public static void install(final Ruby runtime, final LogHandler logHandler) {

    final RubyModule asciidoctorModule = runtime.getModule("Asciidoctor");
    final RubyModule loggerManager = asciidoctorModule.defineOrGetModuleUnder("LoggerManager");
    final RubyClass loggerBaseClass = asciidoctorModule.getClass("Logger");

    final RubyClass loggerClass = asciidoctorModule
        .defineOrGetModuleUnder("LoggerManager")
        .defineClassUnder("JavaLogger", loggerBaseClass, new ObjectAllocator() {
          @Override
          public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
            return new JavaLogger(runtime, klazz, logHandler);
          }
        });

    loggerClass.defineAnnotatedMethods(JavaLogger.class);

    final IRubyObject logger = loggerClass.allocate();
    logger.callMethod(runtime.getCurrentContext(), "initialize", runtime.getNil());
    loggerManager.callMethod("logger=", logger);

  }

  private JavaLogger(final Ruby runtime, final RubyClass metaClass, final LogHandler rootLogHandler) {
    super(runtime, metaClass);
    this.rootLogHandler = rootLogHandler;
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
    if (args.length >= 2 && !args[1].isNil()) {
      rubyMessage = args[1];
    } else if (block.isGiven()) {
      rubyMessage = block.yield(threadContext, getRuntime().getNil());
    } else {
      rubyMessage = args[2];
    }
    final Cursor cursor = getSourceLocation(rubyMessage);
    final String message = formatMessage(rubyMessage);
    final Severity severity = mapRubyLogLevel(args[0]);

    final LogRecord record = createLogRecord(threadContext, severity, cursor, message);

    rootLogHandler.log(record);
    return getRuntime().getNil();
  }

  @JRubyMethod(name = "fatal", required = 0, optional = 2)
  public IRubyObject fatal(final ThreadContext threadContext, final IRubyObject[] args, Block block) {
    return log(threadContext, args, block, Severity.FATAL);
  }

  @JRubyMethod(name = "fatal?")
  public IRubyObject fatal(final ThreadContext threadContext) {
    return getRuntime().getTrue();
  }

  @JRubyMethod(name = "error", required = 0, optional = 2)
  public IRubyObject error(final ThreadContext threadContext, final IRubyObject[] args, Block block) {
    return log(threadContext, args, block, Severity.ERROR);
  }

  @JRubyMethod(name = "error?")
  public IRubyObject error(final ThreadContext threadContext) {
    return getRuntime().getTrue();
  }

  @JRubyMethod(name = "warn", required = 0, optional = 2)
  public IRubyObject warn(final ThreadContext threadContext, final IRubyObject[] args, Block block) {
    return log(threadContext, args, block, Severity.WARN);
  }

  @JRubyMethod(name = "warn?")
  public IRubyObject warn(final ThreadContext threadContext) {
    return getRuntime().getTrue();
  }

  @JRubyMethod(name = "info", required = 0, optional = 2)
  public IRubyObject info(final ThreadContext threadContext, final IRubyObject[] args, Block block) {
    return log(threadContext, args, block, Severity.INFO);
  }

  @JRubyMethod(name = "info?")
  public IRubyObject info(final ThreadContext threadContext) {
    return getRuntime().getTrue();
  }

  @JRubyMethod(name = "debug", required = 0, optional = 2)
  public IRubyObject debug(final ThreadContext threadContext, final IRubyObject[] args, Block block) {
    return log(threadContext, args, block, Severity.DEBUG);
  }

  @JRubyMethod(name = "debug?")
  public IRubyObject debug(final ThreadContext threadContext) {
    return getRuntime().getTrue();
  }


  private IRubyObject log(ThreadContext threadContext, IRubyObject[] args, Block block, Severity severity) {
    final IRubyObject rubyMessage;
    if (block.isGiven()) {
      rubyMessage = block.yield(threadContext, getRuntime().getNil());
    } else {
      rubyMessage = args[0];
    }
    final Cursor cursor = getSourceLocation(rubyMessage);
    final String message = formatMessage(rubyMessage);

    final LogRecord record = createLogRecord(threadContext, severity, cursor, message);

    rootLogHandler.log(record);
    return getRuntime().getNil();
  }


  private LogRecord createLogRecord(final ThreadContext threadContext,
                                    final Severity severity,
                                    final Cursor cursor,
                                    final String message) {
    final Optional<BacktraceElement> elem = threadContext.getBacktrace(0)
            .skip(1)
            .findFirst();

    final String sourceFileName = elem.map(BacktraceElement::getFilename).orElse(null);
    final String sourceMethodName = elem.map(BacktraceElement::getMethod).orElse(null);
    final LogRecord record = new LogRecord(severity, cursor, message, sourceFileName, sourceMethodName);
    return record;
  }

  private Severity mapRubyLogLevel(IRubyObject arg) {
    final int rubyId = arg.convertToInteger().getIntValue();
    switch (rubyId) {
      case 0:
        return Severity.DEBUG;
      case 1:
        return Severity.INFO;
      case 2:
        return Severity.WARN;
      case 3:
        return Severity.ERROR;
      case 4:
        return Severity.FATAL;
      default:
        return Severity.UNKNOWN;
    }
  }

  private String formatMessage(final IRubyObject msg) {
    if (getRuntime().getString().equals(msg.getType())) {
      return msg.asJavaString();
    } else if (getRuntime().getHash().equals(msg.getType())) {
      final RubyHash hash = (RubyHash) msg;
      return Objects.toString(hash.get(getRuntime().newSymbol(LOG_PROPERTY_TEXT)));
    }
    throw new IllegalArgumentException(Objects.toString(msg));
  }

  private Cursor getSourceLocation(IRubyObject msg) {
    if (getRuntime().getHash().equals(msg.getType())) {
      final RubyHash hash = (RubyHash) msg;
      final Object sourceLocation = hash.get(getRuntime().newSymbol(LOG_PROPERTY_SOURCE_LOCATION));
      if (sourceLocation != null) {
        return new CursorImpl((IRubyObject) sourceLocation);
      }
    }
    return null;
  }
}