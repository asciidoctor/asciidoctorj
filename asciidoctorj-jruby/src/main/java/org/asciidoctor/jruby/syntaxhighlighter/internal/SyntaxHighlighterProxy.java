package org.asciidoctor.jruby.syntaxhighlighter.internal;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.internal.Extensions;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.RubyHashMapDecorator;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.asciidoctor.log.Logging;
import org.asciidoctor.syntaxhighlighter.HighlightResult;
import org.asciidoctor.syntaxhighlighter.Highlighter;
import org.asciidoctor.syntaxhighlighter.Formatter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;
import org.asciidoctor.syntaxhighlighter.StylesheetWriter;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class SyntaxHighlighterProxy extends RubyObject {

  protected static final String METHOD_NAME_INITIALIZE = "initialize";
  private final Class<? extends SyntaxHighlighterAdapter> highlighterClass;
  private final JRubyAsciidoctor asciidoctor;

  public static <T extends SyntaxHighlighterAdapter> RubyClass register(JRubyAsciidoctor asciidoctor, final Class<T> highlighterClass) {
    Ruby rubyRuntime = asciidoctor.getRubyRuntime();
    RubyModule module = rubyRuntime.defineModule(getModuleName(highlighterClass));

    RubyClass syntaxHighlighterBase = rubyRuntime.getModule("Asciidoctor")
        .getModule("SyntaxHighlighter")
        .getClass("Base");

    RubyClass clazz = module.defineClassUnder(
        highlighterClass.getSimpleName(),
        syntaxHighlighterBase,
        new SyntaxHighlighterProxy.Allocator(highlighterClass, asciidoctor));

    clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "initialize");
    clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "hasDocInfo");
    clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "getDocInfo");

    if (StylesheetWriter.class.isAssignableFrom(highlighterClass)) {
      clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "isWriteStylesheet");
      clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "writeStylesheet");
    }

    if (Highlighter.class.isAssignableFrom(highlighterClass)) {
      clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "isHighlight");
      clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "highlight");
    }

    if (Formatter.class.isAssignableFrom(highlighterClass)) {
      clazz.defineAnnotatedMethod(SyntaxHighlighterProxy.class, "format");
    }

    return clazz;
  }

  private SyntaxHighlighterAdapter delegate;

  public static class Allocator implements ObjectAllocator {
    private final Class<? extends SyntaxHighlighterAdapter> syntaxHighlighterClass;
    private final JRubyAsciidoctor asciidoctor;

    public Allocator(Class<? extends SyntaxHighlighterAdapter> syntaxHighlighterClass, JRubyAsciidoctor asciidoctor) {
      this.syntaxHighlighterClass = syntaxHighlighterClass;
      this.asciidoctor = asciidoctor;
    }

    @Override
    public IRubyObject allocate(Ruby runtime, RubyClass rubyClass) {
      return new SyntaxHighlighterProxy(runtime, rubyClass, syntaxHighlighterClass, asciidoctor);
    }

    public Class<? extends SyntaxHighlighterAdapter> getSyntaxHighlighterClass() {
      return syntaxHighlighterClass;
    }
  }


  public SyntaxHighlighterProxy(Ruby runtime, RubyClass metaClass, Class<? extends SyntaxHighlighterAdapter> highlighterClass, JRubyAsciidoctor asciidoctor) {
    super(runtime, metaClass);
    this.highlighterClass = highlighterClass;
    this.asciidoctor = asciidoctor;
  }


  @JRubyMethod(required = 1, optional = 2)
  public IRubyObject initialize(ThreadContext context, IRubyObject[] args) {
    Ruby runtime = context.getRuntime();
    try {
      String name = (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[0], String.class);
      RubyString backendRuby = args.length < 2 || args[1].isNil() ? runtime.newString("html5") : (RubyString) args[1];
      String backend = backendRuby.asJavaString();
      RubyHash optionsRuby = args.length < 2 || args[1].isNil() ? new RubyHash(runtime) : (RubyHash) args[2];
      final RubyHashMapDecorator options = new RubyHashMapDecorator(optionsRuby);

      Constructor<? extends SyntaxHighlighterAdapter> constructor = Extensions.findConstructorWithMostMatchingArguments(highlighterClass, name, backend, options);
      switch (constructor.getParameterCount()) {
        case 0:
          this.delegate = constructor.newInstance();
          break;
        case 1:
          this.delegate = constructor.newInstance(name);
          break;
        case 2:
          this.delegate = constructor.newInstance(name, backend);
          break;
        case 3:
          this.delegate = constructor.newInstance(name, backend, options);
          break;
        default:
          throw new IllegalStateException("Expected constructor with 0-3 parameters");
      }
      if (this.delegate instanceof Logging) {
        ((Logging) this.delegate).setLogHandler(asciidoctor);
      }
      // Initialize the Ruby part and pass in the config options
      Helpers.invokeSuper(context, this, getMetaClass(), METHOD_NAME_INITIALIZE, new IRubyObject[]{args[0], backendRuby, optionsRuby}, org.jruby.runtime.Block.NULL_BLOCK);

      return null;
    } catch (InstantiationException e) {
      // TODO: Do some proper logging in the catch clauses?
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @JRubyMethod(name = "docinfo?", required = 1)
  public IRubyObject hasDocInfo(ThreadContext context, IRubyObject location) {
    Ruby runtime = context.getRuntime();
    LocationType locationType = "footer".equals(location.asJavaString()) ? LocationType.FOOTER : LocationType.HEADER;
    return runtime.newBoolean(delegate.hasDocInfo(locationType));
  }

  @JRubyMethod(name = "docinfo", required = 3)
  public IRubyObject getDocInfo(ThreadContext context, IRubyObject location, IRubyObject document, IRubyObject opts) {
    LocationType locationType = "footer".equals(location.asJavaString()) ? LocationType.FOOTER : LocationType.HEADER;
    String result = delegate.getDocinfo(locationType, (Document) NodeConverter.createASTNode(document), RubyHashUtil.convertRubyHashMapToStringObjectMap((RubyHash) opts));
    return context.getRuntime().newString(result);
  }

  @JRubyMethod(name = "write_stylesheet?", required = 1)
  public IRubyObject isWriteStylesheet(ThreadContext context, IRubyObject document) {
    Document doc = (Document) NodeConverter.createASTNode(document);
    return getRuntime().newBoolean(((StylesheetWriter) delegate).isWriteStylesheet(doc));
  }

  @JRubyMethod(name = "write_stylesheet", required = 2)
  public IRubyObject writeStylesheet(ThreadContext context, IRubyObject document, IRubyObject to_dir) {
    StylesheetWriter writer = (StylesheetWriter) delegate;
    Document doc = (Document) NodeConverter.createASTNode(document);

    File toDir = new File(to_dir.asJavaString());

    writer.writeStylesheet(doc, toDir);
    return getRuntime().getNil();
  }

  @JRubyMethod(name = "highlight?")
  public IRubyObject isHighlight(ThreadContext context) {
    return getRuntime().getTrue();
  }

  @JRubyMethod(name = "highlight", required = 4)
  public IRubyObject highlight(ThreadContext context, IRubyObject[] args) {
    Highlighter highlighter = (Highlighter) delegate;
    IRubyObject blockRuby = args[0];
    Block block = (Block) NodeConverter.createASTNode(blockRuby);

    IRubyObject sourceRuby = args[1];
    String source = sourceRuby.asJavaString();

    IRubyObject langRuby = args[2];
    String lang = langRuby.asJavaString();

    RubyHash optionsRuby = (RubyHash) args[3];
    Map<String, Object> options = new RubyHashMapDecorator(optionsRuby);

    HighlightResult result = highlighter.highlight(block, source, lang, options);
    if (result.getLineOffset() != null) {
      return getRuntime().newArray(getRuntime().newString(result.getHighlightedSource()), getRuntime().newFixnum(result.getLineOffset()));
    } else {
      return getRuntime().newString(result.getHighlightedSource());
    }
  }

  @JRubyMethod(name = "format", required = 3)
  public IRubyObject format(ThreadContext context, IRubyObject blockRuby, IRubyObject langRuby, IRubyObject optionsRuby) {
    Formatter formatter = (Formatter) delegate;

    Block block = (Block) NodeConverter.createASTNode(blockRuby);
    String lang = langRuby.asJavaString();
    Map<String, Object> options = new RubyHashMapDecorator((RubyHash) optionsRuby);

    String result = formatter.format(block, lang, options);

    return getRuntime().newString(result);
  }


  private static void includeModule(RubyClass clazz, String moduleName, String... moduleNames) {
    RubyModule module = clazz.getRuntime().getModule(moduleName);
    if (moduleNames != null && moduleNames.length > 0) {
      for (String submoduleName : moduleNames) {
        module = module.getModule(submoduleName);
      }
    }
    clazz.includeModule(module);
  }


  private static String getModuleName(Class<?> highlighterClass) {
    StringBuilder sb = new StringBuilder();
    for (String s : highlighterClass.getPackage().getName().split("\\.")) {
      sb
          .append(s.substring(0, 1).toUpperCase())
          .append(s.substring(1).toLowerCase());
    }
    return sb.toString();
  }


}
