# frozen_string_literal: true

# Translates an Asciidoctor (Minitest + contest) test file written in Ruby into a JUnit 5 test class.
#
# The translation is best-effort: the generated code covers the common patterns (contexts, tests, heredocs,
# conversions, assertions, node accessors) and must be reviewed. Constructs that cannot be translated are emitted as
# `// FIXME` comments followed by the original Ruby source.
#
# Usage: ruby tools/rb2java.rb path/to/xxx_test.rb [OutputClassName] > XxxTest.java

require 'set'

class Rb2Java
  Node = RubyVM::AbstractSyntaxTree::Node

  # Ruby method name => Java method name (without receiver-based dispatch)
  METHODS = {
    'treeprocessor' => 'treeProcessor', 'treeprocessors?' => 'hasTreeProcessors', 'treeprocessors' => 'getTreeProcessors',
    'extensions?' => 'hasExtensions', 'update' => 'putAll',
    'blocks' => 'getBlocks', 'blocks?' => 'hasBlocks', 'title' => 'getTitle', 'title?' => 'hasTitle',
    'context' => 'getContext', 'attributes' => 'getAttributes', 'id' => 'getId', 'role' => 'getRole',
    'roles' => 'getRoles', 'role?' => 'isRole', 'has_role?' => 'hasRole', 'add_role' => 'addRole',
    'remove_role' => 'removeRole', 'option?' => 'isOption', 'set_option' => 'setOption', 'style' => 'getStyle',
    'level' => 'getLevel', 'numeral' => 'getNumeral', 'number' => 'getNumeral', 'caption' => 'getCaption',
    'captioned_title' => 'getCaptionedTitle', 'content' => 'getContent', 'convert' => 'convert',
    'render' => 'convert', 'lines' => 'getLines', 'source' => 'getSource', 'subs' => 'getSubs', 'sub?' => 'hasSub',
    'items' => 'getItems', 'text' => 'getText', 'text?' => 'hasText', 'marker' => 'getMarker',
    'sections' => 'getSections', 'sections?' => 'hasSections', 'sectname' => 'getSectionName', 'special' => 'isSpecial',
    'numbered' => 'getNumbered', 'index' => 'getIndex', 'sectnum' => 'getSectnum', 'xreftext' => 'getXreftext',
    'reftext' => 'getReftext', 'reftext?' => 'isReftext', 'document' => 'getDocument', 'parent' => 'getParent',
    'parent_document' => 'getParentDocument', 'header' => 'getHeader', 'header?' => 'hasHeader',
    'has_header?' => 'hasHeader', 'doctype' => 'getDoctype', 'backend' => 'getBackend', 'safe' => 'getSafe',
    'base_dir' => 'getBaseDir', 'options' => 'getOptions', 'outfilesuffix' => 'getOutfilesuffix',
    'catalog' => 'getCatalog', 'references' => 'getReferences', 'footnotes' => 'getFootnotes',
    'footnotes?' => 'hasFootnotes', 'callouts' => 'getCallouts', 'counters' => 'getCounters', 'author' => 'getAuthor',
    'authors' => 'getAuthors', 'revdate' => 'getRevdate', 'notitle' => 'isNotitle', 'noheader' => 'isNoheader',
    'nofooter' => 'isNofooter', 'docinfo' => 'getDocinfo', 'converter' => 'getConverter',
    'syntax_highlighter' => 'getSyntaxHighlighter', 'extensions' => 'getExtensions',
    'set_attribute' => 'setDocumentAttribute', 'delete_attribute' => 'deleteDocumentAttribute',
    'set_attr' => 'setAttribute', 'remove_attr' => 'removeAttribute', 'set_header_attribute' => 'setHeaderAttribute',
    'parsed?' => 'isParsed', 'nested?' => 'isNested', 'embedded?' => 'isEmbedded', 'basebackend?' => 'isBasebackend',
    'compat_mode' => 'isCompatMode', 'sourcemap' => 'isSourcemap', 'source_location' => 'getSourceLocation',
    'file' => 'getFile', 'lineno' => 'getLineNumber', 'dir' => 'getDir', 'path' => 'getPath', 'has_more_lines?' => 'hasMoreLines',
    'peek_line' => 'peekLine', 'read_line' => 'readLine', 'read_lines' => 'readLines', 'readlines' => 'readLines',
    'skip_blank_lines' => 'skipBlankLines', 'unshift_line' => 'unshiftLine', 'unshift_lines' => 'unshiftLines',
    'restore_line' => 'restoreLine', 'restore_lines' => 'restoreLines', 'cursor' => 'cursor', 'eof?' => 'isEof',
    'empty?' => 'isEmpty', 'columns' => 'getColumns', 'rows' => 'getRows', 'head' => 'getHead', 'body' => 'getBody',
    'foot' => 'getFoot', 'colspan' => 'getColspan', 'rowspan' => 'getRowspan', 'inner_document' => 'getInnerDocument',
    'has_header_option' => 'hasHeaderOption', 'type' => 'getType', 'target' => 'getTarget', 'alt' => 'getAlt',
    'name' => 'getName', 'node_name' => 'getNodeName', 'content_model' => 'getContentModel',
    'next_adjacent_block' => 'getNextAdjacentBlock', 'doctitle' => 'getDoctitle', 'first_section' => 'getFirstSection',
    'messages' => 'getMessages', 'max_severity' => 'getMaxSeverity', 'level=' => 'setLevel', 'block?' => 'isBlock',
    'inline?' => 'isInline', 'list_marker_keyword' => 'getListMarkerKeyword', 'enabled_options' => 'getEnabledOptions',
    'simple?' => 'isSimple', 'compound?' => 'isCompound', 'outline?' => 'isOutline', 'refs' => 'getRefs',
    'images' => 'getImages', 'links' => 'getLinks', 'includes' => 'getIncludes', 'line_info' => 'getLineInfo',
    'process_lines' => 'isProcessLines', 'include_depth' => 'getIncludeDepth', 'include_stack' => 'getIncludeStack',
    'unterminated' => 'getUnterminated', 'source_lines' => 'getSourceLines', 'string' => 'toString',
    'counter' => 'counter', 'register' => 'register', 'resolve_id' => 'resolveId', 'instance' => 'getInstance',
    'config' => 'getConfig', 'kind' => 'getKind', 'groups' => 'getGroups', 'preprocessors' => 'getPreprocessors',
    'tree_processors' => 'getTreeProcessors', 'postprocessors' => 'getPostprocessors',
    'include_processors' => 'getIncludeProcessors', 'docinfo_processors' => 'getDocinfoProcessors',
    'inline_macros' => 'getInlineMacros', 'preprocessors?' => 'hasPreprocessors',
    'tree_processors?' => 'hasTreeProcessors', 'postprocessors?' => 'hasPostprocessors',
    'include_processors?' => 'hasIncludeProcessors', 'docinfo_processors?' => 'hasDocinfoProcessors',
    'blocks_?' => 'hasBlocks', 'block_macros?' => 'hasBlockMacros', 'inline_macros?' => 'hasInlineMacros',
    'registered_for_block?' => 'isRegisteredForBlock', 'registered_for_block_macro?' => 'isRegisteredForBlockMacro',
    'registered_for_inline_macro?' => 'isRegisteredForInlineMacro', 'find_block_extension' => 'findBlockExtension',
    'find_block_macro_extension' => 'findBlockMacroExtension', 'find_inline_macro_extension' => 'findInlineMacroExtension',
    'process_method' => 'getInstance', 'regexp' => 'getRegexp', 'mantitle' => 'getMantitle',
    'main' => 'getMain', 'subtitle' => 'getSubtitle', 'subtitle?' => 'hasSubtitle', 'combined' => 'getCombined',
    'sanitized?' => 'isSanitized', 'firstname' => 'getFirstName', 'middlename' => 'getMiddleName', 'lastname' => 'getLastName',
    'initials' => 'getInitials', 'email' => 'getEmail', 'value' => 'getValue', 'negate' => 'isNegate',
    'basebackend' => 'getBasebackend', 'filetype' => 'getFiletype', 'htmlsyntax' => 'getHtmlsyntax',
    'supports_templates?' => 'isSupportsTemplates', 'handles?' => 'handles', 'converters' => 'getConverters',
    'sanitized' => 'isSanitized', 'working_dir' => 'getWorkingDir', 'file_separator' => 'getFileSeparator',
    'mark' => 'mark', 'terms' => 'getTerms', 'description' => 'getDescription', 'list' => 'getList',
    'column' => 'getColumn', 'table' => 'getTable', 'severity' => 'severity', 'message' => 'message',
    'header_row?' => 'getHeaderRow', 'format' => 'getFormat', 'delimiter' => 'getDelimiter', 'colcount' => 'getColcount',
    'buffer' => 'getBuffer', 'cell_open?' => 'isCellOpen', 'default_subs' => 'getDefaultSubs',
    'logger' => 'getLogger', 'progname' => 'getProgname', 'formatter' => 'getFormatter',
    'next_line_empty?' => 'isNextLineEmpty', 'peek_lines' => 'peekLines', 'skip_comment_lines' => 'skipCommentLines',
    'skip_line_comments' => 'skipLineComments', 'read' => 'read', 'advance' => 'advance', 'terminate' => 'terminate',
    'replace_next_line' => 'replaceNextLine', 'replace_line' => 'replaceNextLine', 'push_include' => 'pushInclude',
    'shift' => 'shift', 'unshift' => 'unshift', 'cursor_at_mark' => 'cursorAtMark', 'cursor_at_line' => 'cursorAtLine',
    'cursor_before_mark' => 'cursorBeforeMark', 'cursor_at_prev_line' => 'cursorAtPrevLine',
    'exceeds_max_depth?' => 'exceedsMaxDepth', 'exceeded_max_depth?' => 'exceedsMaxDepth', 'commit_subs' => 'commitSubs',
    'apply_subs' => 'applySubs', 'sub_specialchars' => 'subSpecialchars', 'sub_quotes' => 'subQuotes',
    'sub_attributes' => 'subAttributes', 'sub_replacements' => 'subReplacements', 'sub_macros' => 'subMacros',
    'sub_post_replacements' => 'subPostReplacements', 'sub_callouts' => 'subCallouts',
    'apply_title_subs' => 'applyTitleSubs', 'apply_header_subs' => 'applyHeaderSubs',
    'apply_normal_subs' => 'applyNormalSubs', 'apply_reftext_subs' => 'applyReftextSubs',
    'extract_passthroughs' => 'extractPassthroughs', 'restore_passthroughs' => 'restorePassthroughs',
    'resolve_subs' => 'resolveSubs', 'resolve_block_subs' => 'resolveBlockSubs', 'resolve_pass_subs' => 'resolvePassSubs',
    'expand_subs' => 'expandSubs', 'parse_attributes' => 'parseAttributes', 'image_uri' => 'imageUri',
    'icon_uri' => 'iconUri', 'media_uri' => 'mediaUri', 'normalize_web_path' => 'normalizeWebPath',
    'normalize_system_path' => 'normalizeSystemPath', 'normalize_asset_path' => 'normalizeAssetPath',
    'read_asset' => 'readAsset', 'read_contents' => 'readContents', 'generate_data_uri' => 'generateDataUri',
    'generate_data_uri_from_uri' => 'generateDataUriFromUri', 'passthroughs' => 'passthroughs',
    'playback_attributes' => 'playbackAttributes', 'restore_attributes' => 'restoreAttributes',
    'attribute_locked?' => 'isAttributeLocked', 'resolve_lines_to_highlight' => 'resolveLinesToHighlight',
    'highlight_source' => 'highlightSource', 'sub_source' => 'subSource',
    'increment_and_store_counter' => 'incrementAndStoreCounter', 'counter_increment' => 'incrementAndStoreCounter',
    'assign_caption' => 'assignCaption', 'assign_numeral' => 'assignNumeral', 'reindex_sections' => 'reindexSections',
    'generate_id' => 'generateId', 'update_attributes' => 'updateAttributes', 'find_by' => 'findBy', 'query' => 'findBy',
    'append' => 'append', 'fold_first' => 'foldFirst', 'parse' => 'parse', 'register_for' => 'registerFor',
    'system_path' => 'systemPath', 'web_path' => 'webPath', 'relative_path' => 'relativePath',
    'absolute_path?' => 'isAbsolutePath', 'root?' => 'isRoot', 'unc?' => 'isUnc', 'web_root?' => 'isWebRoot',
    'descends_from?' => 'descendsFrom', 'posixify' => 'posixify', 'posixfy' => 'posixify',
    'expand_path' => 'expandPath', 'partition_path' => 'partitionPath', 'join_path' => 'joinPath',
    'primary_stylesheet_name' => 'getPrimaryStylesheetName', 'primary_stylesheet_data' => 'getPrimaryStylesheetData',
    'embed_primary_stylesheet' => 'embedPrimaryStylesheet', 'write_primary_stylesheet' => 'writePrimaryStylesheet',
    'create' => 'create', 'for' => 'forBackend', 'activate' => 'activate', 'process' => 'process',
    'unregister_all' => 'unregisterAll', 'unregister' => 'unregister', 'nextval' => 'nextval',
    'clear' => 'clear', 'parent=' => 'setParent', 'title=' => 'setTitle', 'context=' => 'setContext',
    'id=' => 'setId', 'style=' => 'setStyle', 'role=' => 'setRole', 'lines=' => 'setLines', 'text=' => 'setText',
    'caption=' => 'setCaption', 'numeral=' => 'setNumeral', 'number=' => 'setNumeral', 'sectname=' => 'setSectionName',
    'special=' => 'setSpecial', 'numbered=' => 'setNumbered', 'content_model=' => 'setContentModel',
    'source_location=' => 'setSourceLocation', 'marker=' => 'setMarker', 'target=' => 'setTarget',
    'sourcemap=' => 'setSourcemap', 'name=' => 'setName', 'colspan=' => 'setColspan', 'rowspan=' => 'setRowspan',
    'file_separator=' => 'setFileSeparator', 'working_dir=' => 'setWorkingDir', 'logger=' => 'setLogger',
    'progname=' => 'setProgname', 'formatter=' => 'setFormatter', 'process_lines=' => 'setProcessLines',
    'has_header_option=' => 'setHasHeaderOption', 'converter_for' => 'converterFor',
  }.freeze

  # Ruby core methods that keep their meaning but need Java equivalents
  CORE = {
    'size' => 'size', 'length' => 'length', 'include?' => 'contains', 'start_with?' => 'startsWith',
    'end_with?' => 'endsWith', 'first' => 'first', 'last' => 'last', 'keys' => 'keySet', 'values' => 'values',
    'key?' => 'containsKey', 'has_key?' => 'containsKey', 'to_s' => 'toString', 'strip' => 'strip',
    'downcase' => 'toLowerCase', 'upcase' => 'toUpperCase', 'to_a' => 'toList', 'dup' => 'copy', 'delete' => 'remove',
    'count' => 'count', 'sort' => 'sort', 'uniq' => 'distinct', 'reverse' => 'reversed', 'compact' => 'compact',
    'merge' => 'merge', 'chomp' => 'chomp', 'rstrip' => 'rstrip', 'lstrip' => 'lstrip', 'to_sym' => 'toString',
    'intern' => 'toString', 'to_i' => 'toInt', 'to_f' => 'toDouble', 'inspect' => 'inspect', 'class' => 'getClass',
    'frozen?' => 'isFrozen', 'is_a?' => 'isA', 'kind_of?' => 'isA', 'instance_of?' => 'isA', 'chars' => 'chars',
    'bytesize' => 'bytesize', 'encoding' => 'encoding', 'gsub' => 'gsub', 'sub' => 'sub', 'split' => 'split',
    'index' => 'indexOf', 'rindex' => 'lastIndexOf', 'slice' => 'slice', 'squeeze' => 'squeeze', 'tr' => 'tr',
    'to_h' => 'toMap', 'any?' => 'any', 'all?' => 'all', 'none?' => 'none', 'push' => 'add', 'concat' => 'addAll',
    'pop' => 'pop', 'join' => 'join', 'map' => 'map', 'select' => 'select', 'filter' => 'select',
    'reject' => 'reject', 'find' => 'find', 'detect' => 'find', 'each' => 'each', 'each_with_index' => 'eachWithIndex',
    'flatten' => 'flatten', 'sum' => 'sum', 'min' => 'min', 'max' => 'max', 'zip' => 'zip', 'times' => 'times',
    'abs' => 'abs', 'freeze' => 'freeze', 'object_id' => 'objectId', 'respond_to?' => 'respondTo',
    'match?' => 'matches', 'match' => 'match', 'scan' => 'scan', 'ord' => 'ord', 'chr' => 'chr',
  }.freeze

  # Helper functions (receiver-less calls) defined in the Ruby test helper or Minitest
  HELPERS = Set.new(%w(
    convert_string convert_string_to_embedded convert_inline_string document_from_string block_from_string
    empty_document empty_safe_document sample_doc_path fixture_path example_document xmlnodes_at_css
    xmlnodes_at_xpath xmlnodes_at_path contains_class assert_css assert_xpath assert_path assert_include
    refute_include assert_message assert_messages xmldoc_from_string parse_header_metadata decode_char
    invoke_cli_with_filenames invoke_cli_to_buffer invoke_cli redirect_streams resolve_localhost
    using_memory_logger in_verbose_mode asciidoctor_cmd run_command using_test_webserver testdir fixturedir
    bindir disk_root windows? jruby? assign_default_test_options assert_equal refute_equal assert_nil refute_nil
    assert_includes refute_includes assert_empty refute_empty assert_kind_of refute_kind_of assert_instance_of
    assert_match refute_match assert_same refute_same assert_raises assert_path_exists refute_path_exists
    assert_predicate refute_predicate assert_output skip flunk pass assert_operator assert_in_delta
  ))

  attr_reader :out

  def initialize source, class_name
    @source = source
    @source_lines = source.lines
    @class_name = class_name
    @out = []
    @indent = 0
    @scopes = []
    @method_names = []
    @used_names = []
    @fields = {}
  end

  def translate
    ast = RubyVM::AbstractSyntaxTree.parse @source
    emit 'package org.asciidoctor.core;'
    emit ''
    emit 'import static org.junit.jupiter.api.Assertions.*;'
    emit ''
    emit 'import java.util.*;'
    emit ''
    emit 'import org.junit.jupiter.api.*;'
    emit ''
    body = ast.children[2]
    statements = block_statements body
    contexts = statements.select {|s| iter_call?(s, 'context') }
    collect_fields ast
    @test_classes = statements.select {|st| node?(st) && st.type == :CLASS }.map {|st| const_name st.children[0] }
    if contexts.size == 1
      top = contexts[0]
      name = string_value(call_args(iter_call(top))[0])
      emit %(@DisplayName(#{java_string name}))
      emit %(public class #{@class_name} extends #{base_class} {)
      with_indent do
        statements.select {|s| node?(s) && s.type == :CDECL }.each {|s| translate_member s }
        @fields.each {|fname, ftype| emit %(#{ftype} #{fname};) }
        translate_context_body iter_body(top)
      end
      emit '}'
    else
      emit %(public class #{@class_name} extends #{base_class} {)
      with_indent do
        @fields.each {|fname, ftype| emit %(#{ftype} #{fname};) }
        statements.each {|s| translate_member s }
      end
      emit '}'
    end
    add_imports @out.join("\n") + "\n"
  end

  OPTIONAL_IMPORTS = {
    /\bPattern\./ => 'java.util.regex.Pattern',
    /\bnew Invoker\(/ => 'org.asciidoctor.cli.Invoker',
    /\bnew StringWriter\(/ => 'java.io.StringWriter',
    /\bnew StringReader\(/ => 'java.io.StringReader',
    /\bPath\.of\(/ => 'java.nio.file.Path',
    /\bZonedDateTime\./ => 'java.time.ZonedDateTime',
    /@EnabledOnOs\b/ => 'org.junit.jupiter.api.condition.EnabledOnOs',
    /\bOS\.WINDOWS\b/ => 'org.junit.jupiter.api.condition.OS',
    /\bZoneOffset\./ => 'java.time.ZoneOffset',
    /\bStandardCharsets\./ => 'java.nio.charset.StandardCharsets',
    /\bExtensions\./ => 'org.asciidoctor.core.extension.Extensions',
    /\bConverterFactory\b/ => 'org.asciidoctor.core.converter.ConverterFactory',
    /\bConverter\./ => 'org.asciidoctor.core.converter.Converter',
  }

  # Adds the imports for the types referenced by the generated code (besides java.util).
  def add_imports src
    if src.include?('assumeTrue(') && !src.include?('import static org.junit.jupiter.api.Assumptions.assumeTrue;')
      src = src.sub 'import static org.junit.jupiter.api.Assertions.*;', "import static org.junit.jupiter.api.Assertions.*;\nimport static org.junit.jupiter.api.Assumptions.assumeTrue;"
    end
    imports = OPTIONAL_IMPORTS.select {|rx, _| rx.match? src }.values.sort
    return src if imports.empty?
    java_imports, other_imports = imports.partition {|i| i.start_with? 'java.' }
    replacement = (['import java.util.*;'] + java_imports.map {|i| %(import #{i};) }).join("\n")
    replacement += "\n\n" + other_imports.map {|i| %(import #{i};) }.join("\n") unless other_imports.empty?
    src.sub 'import java.util.*;', replacement
  end

  private

  def collect_fields node
    return unless node? node
    if node.type == :IASGN
      fname = node.children[0].to_s.delete('@')
      value = node.children[1]
      ftype = 'Object'
      if node?(value) && value.type == :ATTRASGN && (attr_args = list_items(value.children[2])).size == 1
        value = attr_args[0]
      end
      if node?(value) && value.type == :CALL && value.children[1] == :new && const_name(value.children[0])
        ftype = java_const(const_name(value.children[0]))
      elsif node?(value) && value.type == :FCALL && value.children[0].to_s.start_with?('convert_')
        ftype = 'String'
      elsif node?(value) && value.type == :FCALL && value.children[0].to_s == 'document_from_string'
        ftype = 'Document'
      end
      @fields[camel fname] ||= ftype
    elsif node.type == :LASGN && @in_context_body
      # handled by translate_member
    end
    node.children.each {|c| collect_fields c }
  end

  def emit line
    # the declarations hoisted from the expressions of the line are emitted first
    unless (@hoisted || []).empty?
      pending = @hoisted.dup
      @hoisted.clear
      pending.each {|decl| emit decl }
    end
    @out << (line.empty? ? '' : ('    ' * @indent) + line)
  end

  def with_indent
    @indent += 1
    yield
  ensure
    @indent -= 1
  end

  def node? n
    Node === n
  end

  def block_statements n
    return [] unless n
    n.type == :BLOCK ? n.children : [n]
  end

  def iter_call? n, name = nil
    return false unless node?(n) && n.type == :ITER
    call = n.children[0]
    return false unless node?(call) && call.type == :FCALL
    name.nil? || call.children[0].to_s == name
  end

  def iter_call n
    n.children[0]
  end

  def iter_body n
    scope = n.children[1]
    scope.children[2]
  end

  def iter_params n
    scope = n.children[1]
    scope.children[0].first(arity_of(scope))
  end

  # Returns the names of the destructured block parameters (e.g., |(name, value)|), if any.
  def destructured_params n
    scope = n.children[1]
    args = scope.children[1]
    return unless node?(args) && node?(masgn = args.children[1]) && masgn.type == :MASGN
    list_items(masgn.children[1]).map {|it| it.children[0] }
  end

  def arity_of scope
    args = scope.children[1]
    return 0 unless node?(args)
    args.children[0] || 0
  end

  def call_args call
    list = call.type == :FCALL ? call.children[1] : call.children[2]
    list_items list
  end

  def list_items list
    return [] unless node? list
    case list.type
    when :LIST then list.children.compact
    when :ZLIST then []
    when :SPLAT, :ARGSCAT, :ARGSPUSH then [list]
    else [list]
    end
  end

  def string_value n
    case n.type
    when :STR then n.children[0]
    when :LIT then n.children[0].to_s
    else source_of(n)
    end
  end

  def source_of n
    lines = @source_lines[(n.first_lineno - 1)..(n.last_lineno - 1)]
    return '' unless lines
    if lines.size == 1
      lines[0][n.first_column...n.last_column]
    else
      ([lines[0][n.first_column..]] + lines[1..-2] + [lines[-1][0...n.last_column]]).join
    end
  end

  def translate_context_body body
    block_statements(body).each {|s| translate_member s }
  end

  def translate_member s
    if iter_call?(s, 'context') || iter_call?(s, 'describe')
      name = string_value(call_args(iter_call(s))[0])
      cls = unique_name(class_name_for(name))
      emit ''
      emit '@Nested'
      emit %(@DisplayName(#{java_string name}))
      emit %(class #{cls} {)
      with_indent { translate_context_body iter_body(s) }
      emit '}'
    elsif iter_call?(s, 'test') || iter_call?(s, 'should')
      name = string_value(call_args(iter_call(s))[0])
      meth = unique_name(method_name_for(name))
      condition = test_condition call_args(iter_call(s))[1]
      emit ''
      emit '@Test'
      emit %(@Disabled("JRuby only")) if condition == 'isJruby()'
      emit %(@EnabledOnOs(OS.WINDOWS)) if condition == 'isWindows()'
      emit %(@DisplayName(#{java_string name}))
      emit %(void #{meth}() throws Exception {)
      with_indent do
        emit %(assumeTrue(#{condition});) if condition && !%w(isJruby() isWindows()).include?(condition)
        # NOTE the body of a JRuby-only test is not translated since it relies on features specific to JRuby
        unless condition == 'isJruby()'
          @ref_vars = ref_vars_in iter_body(s)
          with_scope { translate_statements iter_body(s) }
          @ref_vars = nil
        end
      end
      emit '}'
    elsif node?(s) && s.type == :DEFN && %w(setup teardown).include?(s.children[0].to_s)
      kind = s.children[0].to_s
      emit ''
      emit(kind == 'setup' ? '@BeforeEach' : '@AfterEach')
      emit %(void #{kind}() throws Exception {)
      with_indent { with_scope { translate_statements s.children[1].children[2] } }
      emit '}'
    elsif node?(s) && s.type == :DEFN
      name = s.children[0].to_s
      scope = s.children[1]
      params = scope.children[0].first(arity_of(scope)) rescue []
      emit ''
      emit %(// FIXME review helper translated from Ruby: def #{name})
      emit %(Object #{camel name}(#{params.map {|p| "Object #{camel p}" }.join ', '}) throws Exception {)
      with_indent do
        with_scope(params) { translate_statements scope.children[2], true }
      end
      emit '}'
    elsif iter_call?(s, 'setup') || iter_call?(s, 'teardown')
      kind = iter_call(s).children[0].to_s
      emit ''
      emit(kind == 'setup' ? '@BeforeEach' : '@AfterEach')
      emit %(void #{kind}() throws Exception {)
      with_indent { with_scope { translate_statements iter_body(s) } }
      emit '}'
    elsif node?(s) && (s.type == :FCALL || s.type == :VCALL) && %w(require require_relative).include?(s.children[0].to_s)
      # ignore
    elsif node?(s) && (s.type == :LASGN || s.type == :DASGN || s.type == :DASGN_CURR)
      value = s.children[1]
      ftype = 'Object'
      vsrc = source_of(value)
      ftype = 'Logger' if vsrc =~ /LoggerManager\.logger/
      ftype = 'String' if node?(value) && (value.type == :STR || value.type == :DSTR)
      emit %(#{ftype} #{camel s.children[0]} = #{expr value};)
      declare_global s.children[0]
    elsif node?(s) && (s.type == :CDECL)
      value = s.children[1]
      ctype = if node?(value) && %i(STR DSTR).include?(value.type)
        'String'
      elsif node?(value) && value.type == :CALL && node?(value.children[0]) && %i(STR DSTR).include?(value.children[0].type) &&
          %i(chop chomp strip rstrip lstrip).include?(value.children[1])
        'String'
      elsif node?(value) && value.type == :LIST && value.children.compact.all? {|it| node?(it) && it.type == :STR }
        # NOTE org.asciidoctor.core.List shadows java.util.List in the test package
        'java.util.List<String>'
      elsif source_of(value) =~ /\AASCIIDOCTOR_\w+_DIR\z/
        'String'
      end
      (@string_constants ||= []) << s.children[0] if ctype == 'String'
      (@defined_constants ||= []) << s.children[0].to_s
      emit %(// FIXME constant: #{source_of(s).lines.first.strip}) unless ctype
      emit %(static final #{ctype || 'Object'} #{s.children[0]} = #{expr value};)
    elsif node?(s) && s.type == :CLASS && source_of(s.children[1]) == 'Minitest::Test'
      # the test class itself; translate its body as members
      block_statements(s.children[2].children[2]).each {|m| translate_member m }
    elsif node?(s) && s.type == :CLASS
      emit ''
      emit '// FIXME class defined in Ruby test; port manually'
      source_of(s).each_line {|l| emit %(// #{l.rstrip}) }
    else
      emit %(// FIXME unsupported member)
      source_of(s).each_line {|l| emit %(// #{l.rstrip}) } if node?(s)
    end
  end

  # Returns the Java expression of the condition of a test (e.g., test 'name', if: jruby? do), if any.
  def test_condition opts
    return unless node?(opts) && opts.type == :HASH
    items = list_items(opts.children[0])
    items.each_slice(2) do |k, v|
      return expr(v) if node?(k) && k.type == :LIT && k.children[0] == :if
      return %(!(#{expr v})) if node?(k) && k.type == :LIT && k.children[0] == :unless
    end
    nil
  end

  def unique_name name
    name = 'test' if name.empty?
    candidate = name
    idx = 2
    while @used_names.include? candidate
      candidate = %(#{name}#{idx})
      idx += 1
    end
    @used_names << candidate
    candidate
  end

  # the CLI tests (in the asciidoctorj-java-cli module) extend the class that provides the CLI helpers
  CLI_TESTS = %w(InvokerTest OptionsTest)

  def base_class
    CLI_TESTS.include?(@class_name) ? 'CliTestHelper' : 'TestHelper'
  end

  # the CLI uses the log severities of the AsciidoctorJ API
  def severity_constant level
    CLI_TESTS.include?(@class_name) ? %(org.asciidoctor.log.Severity.#{level}) : %(Logger.Severity.#{level})
  end

  # type names that must not be shadowed by the name of a nested test class
  RESERVED_CLASS_NAMES = (Dir[File.join __dir__, '../asciidoctorj-*/src/{main,test}/java/org/asciidoctor/core/**/*.java'].map {|f| File.basename f, '.java' } +
    %w(Object String Map List Set Integer Long Double Boolean Character Optional Pattern Matcher Path File Files Objects Arrays Collections)).to_set

  def class_name_for name
    words = name.gsub(/\W+/, ' ').strip.split
    result = words.map {|w| w[0].upcase + w[1..] }.join
    result = %(C#{result}) if result =~ /\A\d/
    result = %(#{result}Tests) if RESERVED_CLASS_NAMES.include? result
    result.empty? ? 'Context' : result
  end

  def method_name_for name
    words = name.gsub(/\W+/, ' ').strip.split
    return 'test' if words.empty?
    result = words[0].downcase + words[1..].map {|w| w[0].upcase + w[1..] }.join
    result = %(test#{result}) if result =~ /\A\d/
    result = %(#{result}_) if JAVA_KEYWORDS.include? result
    result
  end

  JAVA_KEYWORDS = Set.new(%w(abstract assert boolean break byte case catch char class const continue default do double
    else enum extends final finally float for goto if implements import instanceof int interface long native new
    package private protected public return short static strictfp super switch synchronized this throw throws
    transient try void volatile while var record yield true false null))

  # numbers the unused parameters when there are several of them (e.g., (ignored1, ignored2, attrs))
  def unique_params ps
    return ps if ps.count('ignored') < 2
    n = 0
    ps.map {|p| p == 'ignored' ? "ignored#{n += 1}" : p }
  end

  def camel name
    name = name.to_s.sub(/[?!=]\z/, '')
    # an unused parameter (e.g., |_, err|) (NOTE unnamed variables require Java 22)
    return 'ignored' if name == '_'
    name = name.sub(/\A_+/, '')
    parts = name.split('_')
    first = parts.shift || ''
    result = first + parts.map {|p| p.empty? ? '' : p[0].upcase + p[1..] }.join
    JAVA_KEYWORDS.include?(result) ? %(#{result}_) : result
  end

  def with_scope vars = []
    @scopes << Set.new(vars.map(&:to_s))
    yield
  ensure
    @scopes.pop
  end

  def declared? name
    @scopes.any? {|s| s.include? name.to_s } || (@globals || Set.new).include?(name.to_s)
  end

  def declare_global name
    (@globals ||= Set.new) << name.to_s
  end

  def declare name
    @scopes[-1] << name.to_s
  end

  def translate_statements body, returns_last = false
    stmts = block_statements body
    stmts.each_with_index do |s, idx|
      last = returns_last && idx == stmts.size - 1
      if last && node?(s) && s.type == :ATTRASGN
        # the value of a setter is not returned in Java
        statement s
        emit 'return null;'
      elsif last && (expression_node?(s) || value_iter?(s))
        emit %(return #{expr s};)
      elsif last && node?(s) && %i(LASGN DASGN DASGN_CURR IASGN).include?(s.type)
        # the value of an assignment is the assigned value
        statement s
        emit %(return #{ref_var?(s.children[0]) ? camel(s.children[0]) + '.get()' : camel(s.children[0].to_s.delete('@'))};)
      elsif last && node?(s) && %i(IF UNLESS).include?(s.type)
        # the value of the if expression is the value of the block (e.g., the value returned by a lambda)
        if_statement s, true
        emit 'return null;' unless s.children[1] && s.children[2]
      elsif last && node?(s) && s.type == :CASE
        emit 'return null;' unless case_statement s, true
      else
        statement s
      end
    end
  end

  # Translates the assignment of a local variable (the statement form).
  def assign_local name, value
    if value.nil? || value == :NODE_SPECIAL_REQUIRED_KEYWORD
      emit %(// FIXME masgn #{name})
      return
    end
    if node?(value) && value.type == :GVAR && value.children[0] == :$VERBOSE
      # $VERBOSE has no equivalent in Java
      declare name
      return
    end
    if ref_var? name
      # a variable that is assigned in a block (i.e., a lambda) is wrapped in a Ref
      if declared? name
        emit %(#{camel name}.set(#{expr value});)
      else
        declare name
        emit %(var #{camel name} = new Ref<#{value.type == :NIL ? nil_type(name) : ''}>(#{expr value});)
      end
      return
    end
    if declared? name
      cast = name == :para && node?(value) && value.type == :FCALL && value.children[0] == :block_from_string ? '(Block) ' : ''
      emit %(#{camel name} = #{cast}#{expr value};)
    else
      declare name
      if value.type == :NIL
        emit %(#{nil_type name} #{camel name} = null;)
      elsif value.type == :ITER || lambda_node?(value)
        emit %(var #{camel name} = #{expr value};)
      elsif name == :para && value.type == :FCALL && value.children[0] == :block_from_string
        # a paragraph is a Block (block_from_string returns an AbstractBlock)
        emit %(var #{camel name} = (Block) #{expr value};)
      elsif name == :options && value.type == :CALL && value.children[1] == :parse!
        # parse! returns the Options (or the Integer exit code)
        emit %(var #{camel name} = (org.asciidoctor.cli.Options) #{expr value};)
      elsif name == :doc && value.type == :CALL && (%i(convert convert_file render render_file).include?(value.children[1]) ||
          (value.children[1] == :document && source_of(value.children[0]) =~ /invoker/))
        # convert returns the Document when the output is written to a file (otherwise, the converted String)
        emit %(var #{camel name} = (Document) #{expr value};)
      else
        emit %(var #{camel name} = #{expr value};)
      end
    end
  end

  # Translates a case statement to an if/else chain. Returns whether the statement has an else clause.
  def case_statement s, returns_last = false
    subject = expr s.children[0]
    clause = s.children[1]
    first = true
    while node?(clause) && clause.type == :WHEN
      values = list_items(clause.children[0]).map {|v| %(Objects.equals(#{subject}, #{expr v})) }
      emit %(#{first ? '' : '} else '}if (#{values.join ' || '}) {)
      with_indent { translate_statements clause.children[1], returns_last }
      first = false
      clause = clause.children[2]
    end
    if clause
      emit '} else {'
      with_indent { translate_statements clause, returns_last }
    end
    emit '}'
    !clause.nil?
  end

  def if_statement s, returns_last = false
    cond = truthy_expr s.children[0], expr(s.children[0])
    cond = %(!(#{cond})) if s.type == :UNLESS
    emit %(if (#{cond}) {)
    with_indent { translate_statements s.children[1], returns_last }
    if s.children[2]
      if s.children[2].type == :IF && !returns_last
        emit '} else'
        if_statement s.children[2]
        return
      end
      emit '} else {'
      with_indent { translate_statements s.children[2], returns_last }
    end
    emit '}'
  end

  # Returns the names of the local variables of the test that are assigned inside a block (which become lambdas).
  def ref_vars_in body
    outer = []
    inner = []
    collect_assignments body, false, outer, inner
    (outer & inner).to_set
  end

  def collect_assignments node, in_block, outer, inner
    return unless node? node
    if %i(LASGN DASGN DASGN_CURR).include?(node.type)
      (in_block ? inner : outer) << node.children[0]
    end
    if node.type == :ITER
      # NOTE a block passed to each is translated to a loop, so the variables it assigns don't need to be wrapped
      call = node.children[0]
      loop = node?(call) && call.type == :CALL && %i(each each_line each_with_index times each_pair).include?(call.children[1])
      collect_assignments call, in_block, outer, inner
      node.children[1..].each {|c| collect_assignments c, in_block || !loop, outer, inner }
    else
      node.children.each {|c| collect_assignments c, in_block, outer, inner }
    end
  end

  # Infers the type of a variable that is initialized to nil from its name.
  def nil_type name
    case name.to_s
    when /\Adoc/ then 'Document'
    when /logger/ then 'MemoryLogger'
    when /invoker/ then 'Invoker'
    when /attrs|attributes/ then 'Map<String, Object>'
    when /context|style|role|target|id\z/ then 'String'
    when /\Asect/ then 'Section'
    when /output|result|html|text|content|source|input|expected|actual|xml|name|path|file|dir|warning|message|title|value|error/
      'String'
    else 'Object'
    end
  end

  def ref_var? name
    @ref_vars&.include? name
  end

  # Returns the names of the local variables referenced in the node.
  def local_vars_in node, acc = []
    return acc unless node? node
    acc << node.children[0] if %i(LVAR DVAR).include?(node.type)
    node.children.each {|c| local_vars_in c, acc }
    acc
  end

  # NOTE AST nodes are recreated when traversed, so a node is identified by its location
  def node_key n
    [n.type, n.first_lineno, n.first_column, n.last_lineno, n.last_column]
  end

  # the variable declarations hoisted from the expression being translated
  def hoisted
    @hoisted ||= []
  end

  def flush_hoisted
    pending = hoisted.dup
    hoisted.clear
    pending.each {|decl| emit decl }
  end

  # the kind of the enclosing blocks (:loop or :lambda), which determines how next is translated
  def block_kinds
    @block_kinds ||= []
  end

  def within_block kind
    block_kinds.push kind
    yield
  ensure
    block_kinds.pop
  end

  # Returns whether the node is a block that computes a value (e.g., list.map {|it| ... }).
  def value_iter? s
    return false unless node?(s) && s.type == :ITER && node?(call = s.children[0]) && call.type == :CALL
    %i(map collect select filter reject find detect each_with_object inject reduce sort_by group_by tap).include?(call.children[1])
  end

  def expression_node? s
    node?(s) && !%i(LASGN DASGN IASGN IF UNLESS WHILE UNTIL ITER RESCUE ENSURE BEGIN RETURN CASE CASE2 CASE3).include?(s.type)
  end

  def statement s
    return unless node? s
    case s.type
    when :LASGN, :DASGN, :DASGN_CURR
      assign_local s.children[0], s.children[1]
    when :MASGN
      lhs = s.children[1]
      rhs = s.children[0]
      names = list_items(lhs).map {|n| n.children[0] }
      if node?(rhs) && rhs.type == :LIST && list_items(rhs).size == names.size
        # a, b = x, y
        names.zip(list_items(rhs)).each {|n, v| assign_local n, v }
        return
      end
      emit %(var masgn = #{expr rhs};)
      names.each_with_index do |n, i|
        if declared? n
          emit %(#{camel n} = get(masgn, #{i});)
        else
          declare n
          emit %(var #{camel n} = get(masgn, #{i});)
        end
      end
    when :OP_ASGN_OR
      target = s.children[0]
      value = s.children[2]
      name = target.children[0]
      if declared? name
        emit %(if (#{camel name} == null) #{camel name} = #{expr value.children[1]};)
      else
        declare name
        emit %(var #{camel name} = #{expr value.children[1]};)
      end
    when :IASGN
      field = camel s.children[0].to_s.delete('@')
      value = s.children[1]
      if node?(value) && value.type == :ATTRASGN && (attr_args = list_items(value.children[2])).size == 1
        # @a = (obj.b = value) => a = value; obj.setB(a);
        emit %(#{field} = #{expr attr_args[0]};)
        emit %(#{attrasgn_with value, field};)
      else
        emit %(#{field} = #{expr value};)
      end
    when :IF, :UNLESS
      if_statement s
    when :NEXT
      if block_kinds.last == :lambda
        value = s.children[0]
        emit %(return #{node?(value) ? expr(value) : 'null'};)
      else
        emit 'continue;'
      end
    when :ITER
      iter_statement s
    when :BEGIN
      translate_statements s.children[0]
    when :ENSURE
      # the variables assigned in the begin clause are visible in the ensure clause in Ruby, so declare the variables
      # used in the ensure clause before the try statement
      body = s.children[0]
      body_stmts = block_statements(body)
      used = local_vars_in s.children[1]
      leading = body_stmts.take_while {|st| node?(st) && %i(LASGN DASGN DASGN_CURR).include?(st.type) }
                          .select {|st| used.include? st.children[0] }
      leading.each {|st| statement st }
      emit 'try {'
      with_indent { (body_stmts - leading).each {|st| statement st } }
      emit '} finally {'
      with_indent { translate_statements s.children[1] }
      emit '}'
    when :RESCUE
      emit 'try {'
      with_indent { translate_statements s.children[0] }
      emit '} catch (Exception e) {'
      with_indent do
        resbody = s.children[1]
        translate_statements(resbody.children[1]) if node?(resbody)
      end
      emit '}'
    when :RETURN
      emit(s.children[0] ? %(return #{expr s.children[0]};) : 'return;')
    when :WHILE
      emit %(while (#{expr s.children[0]}) {)
      with_indent { translate_statements s.children[1] }
      emit '}'
    when :BLOCK
      translate_statements s
    when :CASE
      case_statement s
    when :DEFN
      # a method defined in a DSL block (e.g., def process parent, reader, attributes) is the equivalent of the DSL method
      frame = dsl_frames.reverse.find {|f| f[0] == :processor }
      if frame && %i(process handles?).include?(s.children[0])
        name = s.children[0].to_s
        scope = s.children[1]
        params = scope.children[0].first(arity_of(scope))
        arity = name == 'process' ? DSL_PROCESSOR_KINDS[frame[2]][1] : 1
        ps = params.map {|p| camel p }
        ps << 'ignored' while ps.size < arity
        ps = unique_params ps
        lambda_params = ps.size == 1 ? ps[0] : %((#{ps.join ', '}))
        emit %(#{frame[1]}.#{dsl_method name}(#{lambda_params} -> #{lambda_body_of params, scope.children[2]});)
      else
        emit %(/* FIXME DEFN */ #{java_string source_of(s)};)
      end
    when :GASGN
      # $VERBOSE is used to silence warnings in Ruby, which has no equivalent in Java
      emit %(/* FIXME GASGN */ #{java_string source_of(s)};) unless s.children[0] == :$VERBOSE
    else
      java = expr s
      flush_hoisted
      # a bare reference (e.g., the result of obj.freeze) is not a statement in Java
      emit %(#{java};) unless java =~ /\A[\w.]+\z|\A\(\(\w+\) (?:\w+\()?[\w.]+\(\)\)?\)\z/
    end
  rescue => e
    emit %(// FIXME translation error (#{e.class}: #{e.message}))
    source_of(s).each_line {|l| emit %(// #{l.rstrip}) }
  end

  def lambda_node? n
    node?(n) && n.type == :LAMBDA
  end

  # Wraps a condition that is not necessarily a boolean (e.g., a variable that may be nil) with truthy().
  def truthy_expr node, java
    if node?(node) && (%i(LVAR DVAR IVAR).include?(node.type) || (node.type == :CALL && node.children[1] == :[]))
      %(isTruthy(#{java}))
    else
      java
    end
  end

  def iter_statement s
    within_block(:loop) { iter_statement_in_block s }
  end

  def iter_statement_in_block s
    if (dsl = dsl_iter s)
      emit %(#{dsl};)
      return
    end
    call = s.children[0]
    params = iter_params s
    body = iter_body s
    if call.type == :FCALL
      name = call.children[0].to_s
      args = call_args(call).map {|a| expr a }
      case name
      when 'using_memory_logger'
        args = call_args(call).map {|a| node?(a) && a.type == :LIT && Symbol === a.children[0] ? %(Logger.Severity.#{a.children[0].to_s.upcase.sub 'WARNING', 'WARN'}) : expr(a) }
        var = params[0] ? camel(params[0]) : 'logger'
        emit %(try (var #{var} = usingMemoryLogger(#{args.join ', '})) {)
        with_indent { with_scope(params) { translate_statements body } }
        emit '}'
        return
      when 'in_verbose_mode'
        emit 'try (var verbose = inVerboseMode()) {'
        with_indent { with_scope(params) { translate_statements body } }
        emit '}'
        return
      when 'redirect_streams'
        emit 'try (var streams = redirectStreams()) {'
        with_indent do
          # NOTE an unused stream (e.g., |_, err|) is still captured
          emit(params[0].to_s == '_' ? 'streams.out();' : %(var #{camel params[0]} = streams.out();)) if params[0]
          emit(params[1].to_s == '_' ? 'streams.err();' : %(var #{camel params[1]} = streams.err();)) if params[1]
          with_scope(params) { translate_statements body }
        end
        emit '}'
        return
      when 'using_test_webserver'
        emit %(try (var server = startTestWebserver(#{args.join ', '})) {)
        with_indent do
          emit %(var #{camel params[0]} = server.getBaseUrl();) if params[0]
          emit %(var #{camel params[1]} = server;) if params[1]
          with_scope(params) { translate_statements body }
        end
        emit '}'
        return
      when 'assert_raises'
        raw = call_args(call)
        classes = args.reject.with_index {|_, i| %i(STR DSTR).include? raw[i].type }
        messages = args.select.with_index {|_, i| %i(STR DSTR).include? raw[i].type }
        lambda_stmt("assertThrows(#{classes.map {|a| class_literal a }.join ', '}, ", [], body,
            suffix: messages.empty? ? ');' : ", #{messages[0]});")
        return
      end
    elsif call.type == :CALL
      recv = call.children[0]
      meth = call.children[1].to_s
      args = call_args(call)
      case meth
      when 'each', 'each_pair'
        if params.size == 2
          emit %(for (var entry : entries(#{node?(recv) && recv.type == :HASH ? smap_literal(recv) : expr(recv)})) {)
          with_indent do
            emit %(var #{camel params[0]} = entry.getKey();)
            emit %(var #{camel params[1]} = entry.getValue();)
            with_scope(params) { translate_statements body }
          end
          emit '}'
          return
        end
        if (names = destructured_params s)
          # |(a, b)| destructures each element
          emit %(for (var it : #{expr recv}) {)
          with_indent do
            names.each_with_index {|nm, i| emit %(var #{camel nm} = get(it, #{i});) }
            with_scope(names) { translate_statements body }
          end
          emit '}'
          return
        end
        var = params[0] ? camel(params[0]) : 'it'
        emit %(for (var #{var} : #{expr recv}) {)
        with_indent { with_scope(params) { translate_statements body } }
        emit '}'
        return
      when 'each_line'
        var = params[0] ? camel(params[0]) : 'it'
        emit %(for (var #{var} : #{expr recv}) {)
        with_indent { with_scope(params) { translate_statements body } }
        emit '}'
        return
      when 'each_with_index'
        var = params[0] ? camel(params[0]) : 'it'
        idx = params[1] ? camel(params[1]) : 'idx'
        emit %(var #{var}List = #{expr recv};)
        emit %(for (int #{idx} = 0; #{idx} < #{var}List.size(); #{idx}++) {)
        with_indent do
          emit %(var #{var} = #{var}List.get(#{idx});)
          with_scope(params) { translate_statements body }
        end
        emit '}'
        return
      when 'times'
        idx = params[0] ? camel(params[0]) : 'i'
        emit %(for (int #{idx} = 0; #{idx} < #{expr recv}; #{idx}++) {)
        with_indent { with_scope(params) { translate_statements body } }
        emit '}'
        return
      when 'mktmpdir'
        lambda_stmt('withTmpdir(', params, body)
        return
      when 'open'
        if const_name(recv) == 'File'
          lambda_stmt("withFile(#{args.map {|a| expr a }.join ', '}, ", params, body)
          return
        end
      when 'register'
        lambda_stmt("#{expr recv}.register(#{args.map {|a| expr(a) + ', ' }.join}", params.empty? ? [:registry] : params, body, true)
        return
      end
    end
    emit %(#{expr s};)
  end

  def lambda_stmt prefix, params, body, registry_dsl = false, suffix: ');'
    ps = params.map {|p| camel p }
    lambda_params = ps.size == 1 ? ps[0] : %((#{ps.join ', '}))
    emit %(#{prefix}#{lambda_params} -> {)
    with_indent { within_block(:lambda) { with_scope(params) { translate_statements body } } }
    emit %(}#{suffix})
  end

  def class_literal java
    java.end_with?('.class') ? java : %(#{java}.class)
  end

  def const_name n
    return nil unless node? n
    case n.type
    when :CONST then n.children[0].to_s
    when :COLON2 then [const_name(n.children[0]), n.children[1]].compact.join('::')
    when :COLON3 then n.children[0].to_s
    end
  end

  KNOWN_ASCIIDOCTOR_CONSTANTS = %w(LF NULL TAB VERSION DEFAULT_BACKEND DEFAULT_DOCTYPE DEFAULT_EXTENSIONS
    DEFAULT_STYLESHEET_KEYS DEFAULT_STYLESHEET_NAME ASCIIDOC_EXTENSIONS DEFAULT_ATTRIBUTES INTRINSIC_ATTRIBUTES
    BACKEND_ALIASES USER_HOME ROOT_DIR LIB_DIR DATA_DIR FILE_READ_MODE FILE_WRITE_MODE URI_READ_MODE
    ADMONITION_STYLES PARAGRAPH_STYLES VERBATIM_STYLES DELIMITED_BLOCKS MAX_INT HIGHLIGHT_JS_VERSION
    MATHJAX_VERSION FONT_AWESOME_VERSION FLEXIBLE_ATTRIBUTES LINE_CONTINUATION LINE_CONTINUATION_LEGACY
    HARD_LINE_BREAK LIST_CONTINUATION)

  def java_const name
    parts = name.split('::')
    parts.shift if parts[0] == 'Asciidoctor' && parts.size > 1
    if parts.size == 1 && KNOWN_ASCIIDOCTOR_CONSTANTS.include?(parts[0])
      return %(Asciidoctor.#{parts[0]})
    end
    if parts.size == 1 && parts[0].end_with?('Rx')
      return %(Rx.#{parts[0].gsub(/([a-z\d])([A-Z])/, '\\1_\\2').upcase})
    end
    case parts.join('::')
    when 'ASCIIDOCTOR_TEST_DIR' then return 'testdir()'
    when 'SecurityError' then return 'SecurityException'
    when 'IOError' then return 'AsciidoctorException'
    when 'NameError' then return 'IllegalArgumentException'
    when 'NoMethodError' then return 'UnsupportedOperationException'
    when 'Proc' then return 'Group'
    when 'LoadError' then return 'IllegalStateException'
    when 'RUBY_DESCRIPTION' then return 'runtimeDescription()'
    when 'File::ALT_SEPARATOR' then return 'altSeparator()'
    when 'ArgumentError' then return 'IllegalArgumentException'
    when 'StandardError', 'RuntimeError' then return 'RuntimeException'
    when 'NotImplementedError' then return 'UnsupportedOperationException'
    when 'Errno::ENOENT' then return 'java.io.UncheckedIOException'
    when 'SafeMode::UNSAFE', 'SafeMode::SAFE', 'SafeMode::SERVER', 'SafeMode::SECURE'
      return %(SafeMode.#{parts[1]}.getLevel())
    when 'Logger::Severity::DEBUG', 'Logger::DEBUG' then return severity_constant('DEBUG')
    when 'Logger::Severity::INFO', 'Logger::INFO' then return severity_constant('INFO')
    when 'Logger::Severity::WARN', 'Logger::WARN' then return severity_constant('WARN')
    when 'Logger::Severity::ERROR', 'Logger::ERROR' then return severity_constant('ERROR')
    when 'Logger::Severity::FATAL', 'Logger::FATAL' then return severity_constant('FATAL')
    when 'Cli::Options' then return 'org.asciidoctor.cli.Options'
    when 'Cli::Invoker' then return 'Invoker'
    when 'Hash' then return 'Map.class'
    when 'Array' then return 'java.util.List.class'
    when 'String' then return 'String.class'
    when 'Extensions::Processor', 'Extensions::Preprocessor', 'Extensions::TreeProcessor', 'Extensions::Treeprocessor',
         'Extensions::Postprocessor', 'Extensions::IncludeProcessor', 'Extensions::DocinfoProcessor',
         'Extensions::BlockProcessor', 'Extensions::BlockMacroProcessor', 'Extensions::InlineMacroProcessor',
         'Extensions::Registry', 'Extensions::Group'
      return parts[1].sub('Treeprocessor', 'TreeProcessor')
    when 'Table::Cell' then return 'Table.Cell'
    when 'Table::Column' then return 'Table.Column'
    when 'Reader::Cursor' then return 'Reader.Cursor'
    when 'Document::Title' then return 'Document.Title'
    when 'Document::Footnote' then return 'Document.Footnote'
    when 'Document::AttributeEntry' then return 'Document.AttributeEntry'
    when 'Converter::Html5Converter' then return 'Html5Converter'
    when 'Converter::DocBook5Converter' then return 'DocBook5Converter'
    when 'Converter::ManPageConverter' then return 'ManPageConverter'
    when 'Converter::CompositeConverter' then return 'CompositeConverter'
    when 'Converter::Base' then return 'AbstractConverter'
    when 'SyntaxHighlighter::Base' then return 'AbstractSyntaxHighlighter'
    end
    parts.join('.')
  end

  def expr n
    return 'null' if n.nil?
    return n.inspect unless node? n
    case n.type
    when :STR then java_string n.children[0], heredoc?(n)
    when :DSTR then dstr n
    when :XSTR then java_string n.children[0]
    when :LIT then lit n.children[0]
    when :NIL then 'null'
    when :TRUE then 'true'
    when :FALSE then 'false'
    when :SELF then (frame = dsl_frames.reverse.find {|f| f[0] == :processor }) ? frame[1] : 'this'
    when :LVAR, :DVAR then ref_var?(n.children[0]) ? %(#{camel n.children[0]}.get()) : camel(n.children[0])
    when :IVAR
      if (frame = dsl_frames.last) && n.children[0] == :@document
        %(#{frame[1]}.getDocument())
      else
        camel n.children[0].to_s.delete('@')
      end
    when :IASGN then %((#{camel n.children[0].to_s.delete('@')} = #{expr n.children[1]}))
    when :GVAR then gvar n.children[0]
    when :CONST, :COLON2, :COLON3
      name = const_name(n) || source_of(n)
      # a class defined by the test (ported as a nested class) is referenced as a value using its class literal
      (@test_classes || []).include?(name) ? %(#{name}.class) : java_const(name)
    when :LIST then list_literal(n)
    when :ZLIST then 'new ArrayList<String>()'
    when :HASH then hash_literal n
    when :VCALL then vcall n.children[0].to_s
    when :FCALL then fcall n
    when :CALL, :QCALL then call n
    when :OPCALL then opcall n
    when :ATTRASGN then attrasgn n
    when :AND then %(#{expr n.children[0]} && #{expr n.children[1]})
    when :OR
      rhs = n.children[1]
      if node?(rhs) && (%i(STR DSTR NIL HASH LIST ZLIST INTEGER FLOAT).include?(rhs.type) ||
          (rhs.type == :LIT && !(TrueClass === rhs.children[0] || FalseClass === rhs.children[0])))
        # the default value idiom (e.g., value || 'default')
        %(orElse(#{expr n.children[0]}, #{expr rhs}))
      else
        %(#{expr n.children[0]} || #{expr rhs})
      end
    when :NOT then %(!(#{expr n.children[0]}))
    when :IF then %((#{truthy_expr n.children[0], expr(n.children[0])} ? #{expr(n.children[1])} : #{expr(n.children[2])}))
    when :UNLESS then %((!(#{expr n.children[0]}) ? #{expr(n.children[1])} : #{expr(n.children[2])}))
    when :DOT2 then %(range(#{expr n.children[0]}, #{expr n.children[1]}))

    when :DOT3 then %(range(#{expr n.children[0]}, #{expr n.children[1]} - 1))
    when :ITER then iter_expr n
    when :LAMBDA then lambda_expr n
    when :DREGX then %(rx(#{dstr n}))
    when :MATCH2, :MATCH3 then %(matches(#{expr n.children[0]}, #{expr n.children[1]}))
    when :SPLAT then %(/* splat */ #{expr n.children[0]})
    when :BLOCK_PASS then %(/* block pass */ #{expr n.children[1]})
    when :EVSTR then expr n.children[0]
    when :LASGN, :DASGN, :DASGN_CURR
      name = n.children[0]
      if (@hoisted_nodes || {})[node_key n]
        # already hoisted (the expression is translated more than once)
        camel name
      elsif ref_var? name
        %(#{camel name}.set(#{expr n.children[1]}))
      elsif declared? name
        %(#{camel name} = #{expr n.children[1]})
      else
        # declare the variable before the statement (e.g., convert_file 'x', timings: (timings = Timings.new))
        declare name
        hoisted << %(var #{camel name} = #{expr n.children[1]};)
        (@hoisted_nodes ||= {})[node_key n] = true
        camel name
      end
    when :BEGIN then n.children[0] ? expr(n.children[0]) : 'null'
    when :BLOCK then block_statements(n).map {|c| expr c }.last
    when :ARGSCAT then %(/* argscat */ #{expr n.children[0]}, #{expr n.children[1]})
    when :DEFINED then %(/* defined? */ (#{expr n.children[0]} != null))
    when :OP_ASGN1, :OP_ASGN2 then %(/* FIXME op_asgn */ #{source_of(n).inspect})
    when :DSYM then dstr n
    when :STR, :DXSTR then java_string source_of(n)
    else
      %(/* FIXME #{n.type} */ #{java_string source_of(n)})
    end
  end

  def heredoc? n
    first_line = @source_lines[n.first_lineno - 1]
    first_line && first_line[n.first_column, 2] == '<<'
  end

  def gvar name
    case name.to_s
    when '$stdout' then 'System.out'
    when '$stderr' then 'System.err'
    when '$VERBOSE' then 'null'
    when '$!' then 'e'
    else %(/* FIXME gvar */ #{name.to_s.delete('$')})
    end
  end

  def lit v
    case v
    when Symbol then java_string v.to_s
    when Integer then v.to_s
    when Float then v.to_s
    when Regexp
      # the m option in Ruby is the equivalent of DOTALL in Java; the i option is the same
      flags = (v.options & Regexp::MULTILINE != 0 ? 'm' : '') + (v.options & Regexp::IGNORECASE != 0 ? 'i' : '')
      flags.empty? ? %(rx(#{java_string v.source})) : %(rx(#{java_string v.source}, "#{flags}"))
    when String then java_string v
    when Range then %(range(#{v.first}, #{v.last}))
    else v.inspect
    end
  end

  def java_string str, prefer_block = false
    str = str.to_s
    if prefer_block && str.end_with?("\n") && str.count("\n") > 1 && !str.include?('"""')
      text_block str
    else
      %("#{escape_java str}")
    end
  end

  def escape_java str
    str.each_char.map do |c|
      case c
      when '\\' then '\\\\'
      when '"' then '\\"'
      when "\n" then '\\n'
      when "\t" then '\\t'
      when "\r" then '\\r'
      when "\b" then '\\b'
      when "\f" then '\\f'
      else
        o = c.ord
        o < 0x20 || o == 0x7f || (o >= 0x80 && o < 0xa0) ? format('\\u%04x', o) : c
      end
    end.join
  end

  def text_block str
    base = '    ' * (@indent + 2)
    lines = str.split("\n", -1)
    lines.pop if lines[-1] == ''
    body = lines.map do |line|
      escaped = line.each_char.map do |c|
        case c
        when '\\' then '\\\\'
        else
          o = c.ord
          (o < 0x20 && c != "\t") || o == 0x7f || (o >= 0x80 && o < 0xa0) ? format('\\u%04x', o) : c
        end
      end.join
      escaped = escaped.gsub('"""', '""\\"')
      # preserve trailing whitespace
      if escaped =~ /[ \t]+\z/
        trailing = $&
        escaped = escaped[0...-trailing.length] + trailing[0...-1].gsub("\t", '\\t') + (trailing[-1] == "\t" ? '\\t' : '\\s')
      end
      line.empty? ? '' : base + escaped
    end
    %("""\n#{body.join "\n"}\n#{base}""")
  end

  def dstr n
    parts = []
    prefix = n.children[0]
    parts << java_string(prefix) unless prefix.nil? || prefix.empty?
    rest = n.children[1..]
    rest.each do |c|
      next unless c
      if node?(c) && c.type == :LIST
        c.children.compact.each {|cc| parts << dstr_part(cc) }
      else
        parts << dstr_part(c)
      end
    end
    parts << '""' if parts.empty?
    parts.size == 1 && parts[0].start_with?('"') ? parts[0] : %((#{parts.join ' + '}))
  end

  def dstr_part c
    return java_string(c) if String === c
    case c.type
    when :STR then java_string c.children[0]
    when :EVSTR then %(str(#{expr c.children[0]}))
    else %(str(#{expr c}))
    end
  end

  def list_literal n
    items = list_items(n).map {|i| expr i }
    # NOTE cast a single null item so it's passed as an element rather than as the varargs array
    items = ['(Object) null'] if items == ['null']
    %(list(#{items.join ', '}))
  end

  def smap_literal n
    items = list_items(n.children[0])
    if items.all? {|i| node?(i) && (i.type == :STR || (i.type == :LIT && (Symbol === i.children[0] || String === i.children[0]))) }
      %(smap(#{items.map {|i| java_string string_value(i) }.join ', '}))
    elsif items.all? {|i| node?(i) && (%i(STR DSTR).include?(i.type) || (i.type == :LIT && (Symbol === i.children[0] || String === i.children[0]))) }
      %(smap(#{items.map {|i| i.type == :LIT ? java_string(i.children[0].to_s) : expr(i) }.join ', '}))
    else
      hash_literal n
    end
  end

  def hash_literal n
    list = n.children[0]
    items = list_items(list)
    pairs = items.each_slice(2).map do |k, v|
      key = if node?(k) && k.type == :LIT && Symbol === k.children[0]
        java_string k.children[0].to_s
      elsif node?(k) && k.type == :LIT && Integer === k.children[0]
        java_string k.children[0].to_s
      else
        expr k
      end
      %(#{key}, #{expr v})
    end
    %(Options.of(#{pairs.join ', '}))
  end

  def vcall name
    if (frame = dsl_frame_for name)
      return %(#{frame[1]}.#{dsl_method name}())
    end
    if HELPERS.include?(name) || %w(testdir fixturedir bindir disk_root).include?(name)
      %(#{camel helper_name(name)}())
    else
      case name
      when 'windows?' then 'isWindows()'
      when 'jruby?' then 'false'
      else %(#{camel name}())
      end
    end
  end

  def helper_name name
    case name
    when 'windows?' then 'is_windows'
    when 'jruby?' then 'is_jruby'
    else name
    end
  end

  def args_of call
    call_args(call).map {|a| expr a }
  end

  SUBSTITUTION_CONSTANTS = { 'specialcharacters' => 'SPECIAL_CHARACTERS', 'specialchars' => 'SPECIAL_CHARACTERS' }

  # Translates the expected value of an assertion to an enum constant (or a list of enum constants) when the actual
  # value is a substitution list or a content model, which are represented by symbols in Ruby.
  def enum_literal expected, actual
    return unless node?(expected) && node?(actual) && %i(CALL QCALL).include?(actual.type)
    meth = actual.children[1]
    if meth == :[] && (key = list_items(actual.children[2])[0]) && key.type == :LIT && key.children[0] == :subs
      meth = :subs
    end
    case meth
    when :subs, :expand_subs, :resolve_subs, :resolve_block_subs, :resolve_pass_subs
      return unless expected.type == :LIST
      items = expected.children.compact
      return unless items.all? {|it| node?(it) && it.type == :LIT && Symbol === it.children[0] }
      %(list(#{items.map {|it| 'Substitution.' + (SUBSTITUTION_CONSTANTS[name = it.children[0].to_s] || name.upcase) }.join ', '}))
    when :numeral, :number
      # the numeral of a block is a String in Java (an Integer in Ruby)
      return unless expected.type == :LIT && Integer === expected.children[0]
      %("#{expected.children[0]}")
    when :content_model
      return unless expected.type == :LIT && Symbol === expected.children[0]
      %(ContentModel.#{expected.children[0].to_s.upcase})
    end
  end

  def fcall n
    name = n.children[0].to_s
    raw_args = call_args(n)
    if (frame = dsl_frame_for name)
      if name == 'prefer' && frame[0] == :registry && node?(raw_args[0]) && raw_args[0].type == :LIT
        java_name, _ = DSL_PROCESSOR_KINDS[raw_args[0].children[0].to_s]
        return %(#{frame[1]}.prefer(#{frame[1]}.#{java_name}(#{raw_args[1..].map {|a| expr a }.join ', '})))
      end
      return %(#{frame[1]}.#{dsl_method name}(#{dsl_args(name, raw_args).join ', '}))
    end
    args = raw_args.map {|a| expr a }
    case name
    when 'run_command'
      %(runCommand(list(#{args.join ', '})))
    when 'assert_message', 'assert_messages'
      # the message of a log message is a Hash in Ruby (i.e., a LogMessage in Java)
      %(#{camel name}(#{args.join(', ').gsub('Map.class', 'LogMessage.class')}))
    when 'assert'
      %(assertTruthy(#{args.join ', '}))
    when 'refute'
      %(assertFalsy(#{args.join ', '}))
    when 'assert_equal'
      args[2] = %(str(#{args[2]})) if args.size > 2 && !(node?(raw_args[2]) && %i(STR DSTR).include?(raw_args[2].type))
      # NOTE a predicate that returns false in Ruby typically returns null in Java (e.g., registered_for_block?)
      if node?(raw_args[0]) && raw_args[0].type == :FALSE && node?(raw_args[1]) && raw_args[1].type == :CALL &&
          raw_args[1].children[1].to_s.end_with?('?') && !raw_args[1].children[1].to_s.end_with?('s?')
        return %(assertFalsy(#{args[1..].join ', '}))
      end
      if (enum_args = enum_literal raw_args[0], raw_args[1])
        args[0] = enum_args
      end
      %(assertEquals(#{args.join ', '}))
    when 'refute_equal'
      %(assertNotEquals(#{args.join ', '}))
    when 'assert_nil'
      %(assertNull(#{args.join ', '}))
    when 'refute_nil'
      %(assertNotNull(#{args.join ', '}))
    when 'assert_kind_of', 'assert_instance_of'
      %(assertInstanceOf(#{class_literal args[0]}, #{args[1..].join ', '}))
    when 'refute_kind_of'
      %(assertNotInstanceOf(#{class_literal args[0]}, #{args[1..].join ', '}))
    when 'assert_same'
      %(assertSame(#{args.join ', '}))
    when 'refute_same'
      %(assertNotSame(#{args.join ', '}))
    when 'puts'
      %(System.out.println(#{args.join ', '}))
    when 'p'
      %(System.out.println(#{args.join ', '}))
    when 'Pathname'
      %(Path.of(#{args[0]}))
    when 'raise'
      if raw_args[0] && source_of(raw_args[0]) == 'StopIteration' && block_kinds.last == :lambda
        # raising StopIteration in the block passed to find_by stops the traversal
        'return "stop"'
      else
        %(throw new RuntimeException(#{args.join ', '}))
      end
    when 'lambda', 'proc'
      %(/* FIXME #{name} */ null)
    when 'xmlnodes_at_xpath', 'xmlnodes_at_css'
      count_node = raw_args[2]
      args = args.first(2) unless node?(count_node) && count_node.type == :LIT && count_node.children[0] == 1
      %(#{camel name}(#{args.join ', '}))
    else
      %(#{camel helper_name(name)}(#{args.join ', '}))
    end
  end

  BLOCK_PASS_ITERATORS = { 'map' => 'map', 'collect' => 'map', 'select' => 'select', 'filter' => 'select',
    'reject' => 'reject', 'find' => 'find', 'detect' => 'find', 'count' => 'count', 'any?' => 'any', 'all?' => 'all',
    'sort_by' => 'sortBy', 'flat_map' => 'flatMap' }

  BLOCK_PASS_METHOD_REFS = { 'chomp' => '(String it) -> Strings.chomp(it)', 'strip' => '(String it) -> Strings.strip(it)',
    'rstrip' => '(String it) -> Strings.rstrip(it)', 'to_s' => 'it -> str(it)', 'to_i' => 'it -> toInt(it)',
    'upcase' => '(String it) -> it.toUpperCase()', 'downcase' => '(String it) -> it.toLowerCase()',
    'empty?' => 'it -> isEmpty(it)', 'size' => 'it -> size(it)', 'length' => 'it -> size(it)' }

  # Translates a block pass (e.g., &:text) to a Java function.
  def block_pass_function n
    sym = n.children[1]
    return unless node?(sym) && sym.type == :LIT && Symbol === sym.children[0]
    name = sym.children[0].to_s
    BLOCK_PASS_METHOD_REFS[name] || %(it -> send(it, #{java_string name}))
  end

  def call n
    recv = n.children[0]
    meth = n.children[1].to_s
    raw_args = call_args(n)
    args = raw_args.map {|a| expr a }
    r = expr recv
    cname = const_name(recv)
    # a constant defined by the test (e.g., SAMPLE_DATA) is a value, not a class
    cname = nil if (@defined_constants || []).include?(cname) || cname =~ /Rx\z/
    if cname
      jc = java_const(cname)
      case meth
      when 'new'
        # Java strings are always Unicode
        return args[0] if cname == 'String' && args.size >= 1
        # a StringIO is used either as an input (with a String) or as an output
        return (args.empty? ? 'new StringWriter()' : %(new StringReader(#{args[0]}))) if cname == 'StringIO'
        return %(Path.of(#{args[0]})) if cname == 'Pathname'
        if cname == 'Time' && args.size == 7
          return %(ZonedDateTime.of(#{args[0..5].join ', '}, 0, ZoneOffset.of(#{args[6]})))
        end
        return %(new #{jc}(#{args.join ', '}))
      when 'read', 'binread'
        # the mode (or encoding) is irrelevant since files are read as UTF-8
        return %(readFile(#{args[0]})) if cname == 'File'
      when 'join'
        return %(joinPath(#{args.join ', '})) if cname == 'File'
      when 'exist?', 'file?', 'directory?', 'readable?'
        return %(fileExists(#{args.join ', '})) if cname == 'File'
      when 'basename', 'dirname', 'extname', 'absolute_path', 'expand_path'
        return %(#{camel 'file_' + meth}(#{args.join ', '})) if cname == 'File'
      when 'pwd'
        return 'Helpers.workingDirectory()' if cname == 'Dir'
      when 'escape', 'quote'
        return %(Pattern.quote(#{args[0]})) if cname == 'Regexp'
      when 'parse'
        # URIs are represented as Strings
        return args[0] if cname == 'URI'
      when '[]'
        return %(Set.of(#{args.join ', '})) if cname == 'Set'
        return %(System.getenv(#{args[0]})) if cname == 'ENV'
      when 'now'
        return 'ZonedDateTime.now()' if cname == 'Time'
      when 'tmpdir'
        return 'System.getProperty("java.io.tmpdir")' if cname == 'Dir'
      when 'cp'
        return %(copyFile(#{args.join ', '})) if cname == 'FileUtils'
      when 'rm', 'rm_f'
        return %(removeFile(#{args[0]})) if cname == 'FileUtils'
      when 'rm_r', 'rm_rf', 'rmdir', 'remove_dir'
        return %(removeDir(#{args[0]})) if cname == 'FileUtils' || cname == 'Dir'
      when 'mkdir', 'mkdir_p'
        return %(makeDir(#{args[0]})) if cname == 'FileUtils' || cname == 'Dir'
      end
      if cname =~ /\A(Asciidoctor::)?(Parser|Helpers|Section|Asciidoctor|AttributeList|Document|Converter|SyntaxHighlighter|Extensions|Compliance|SafeMode|Stylesheets|LoggerManager)\z/ || cname == 'Asciidoctor'
        mapped = static_method(jc, meth)
        return %(#{jc}.#{mapped}(#{args.join ', '}))
      end
      return %(#{jc}.#{camel meth}(#{args.join ', '}))
    end
    if raw_args.size == 1 && raw_args[0].type == :BLOCK_PASS && (fn = block_pass_function raw_args[0])
      helper = BLOCK_PASS_ITERATORS[meth]
      return %(#{helper}(#{r}, #{fn})) if helper
    end
    if %w(message cause backtrace).include?(meth) && args.empty? &&
        (node?(recv) && %i(LVAR DVAR).include?(recv.type) && recv.children[0].to_s =~ /\A(?:ex|e|err|error|exception)\z/ ||
         node?(recv) && recv.type == :GVAR && recv.children[0] == :$! ||
         node?(recv) && %i(CALL).include?(recv.type) && recv.children[1] == :cause)
      # an exception
      return %(#{r}.get#{meth.capitalize}())
    end
    case meth
    when 'parse!'
      # Options#parse! (the class method is translated to Options.parse)
      return %(#{r}.parseArgs(#{args.join ', '}))
    when 'read_output', 'read_error'
      return %(#{r}.#{camel meth}())
    when 'document'
      # the document of the invoker is a Document (or the converted String if the output is not written to a file)
      # NOTE the invoker returns the document of the AsciidoctorJ API, which wraps the document of the implementation
      return %(((Document) unwrap(#{r}.getDocument()))) if source_of(recv) =~ /invoker/
    when 'drop'
      return %(drop(#{r}, #{args[0]}))
    when 'unshift'
      return %(#{r}.add(0, #{args[0]})) if args.size == 1 && source_of(recv) !~ /reader/
    when 'concat'
      return %(concat(#{r}, #{args.join ', '}))
    when 'send', '__send__'
      # the tests are in the same package, so the (protected or package-private) method is accessible
      if node?(raw_args[0]) && raw_args[0].type == :LIT && Symbol === raw_args[0].children[0]
        return %(#{r}.#{camel raw_args[0].children[0].to_s}(#{args[1..].join ', '}))
      end
    when 'source'
      # a block that has source is a Block (but is typed as an AbstractBlock)
      return %(((Block) #{r}).getSource()) if args.empty? && source_of(recv) =~ /blocks\[-?\d+\]\z|\.blocks\.(?:first|last)\z/
    when 'reduce', 'inject', 'sum'
      if raw_args.size == 1 && node?(raw_args[0]) && raw_args[0].type == :LIT && raw_args[0].children[0] == :+
        return %(sum(#{r}))
      end
    when 'rows', 'columns'
      # the receiver is a Table (but is often typed as an AbstractBlock)
      return %(((Table) #{r}).get#{meth.capitalize}()) if args.empty?
    when 'instance_variable_get'
      # the tests are in the same package, so the (protected or package-private) field is accessible
      return %(#{r}.#{camel raw_args[0].children[0].to_s.delete('@')}) if raw_args[0].type == :LIT
    when 'instance_variable_set'
      return %(#{r}.#{camel raw_args[0].children[0].to_s.delete('@')} = #{args[1]}) if raw_args[0].type == :LIT
    when '[]'
      return index_expr(recv, r, raw_args, args)
    when '[]='
      return %(#{r}.put(#{map_key raw_args[0], args[0]}, #{args[1]}))
    when '==' then return %(Objects.equals(#{r}, #{args[0]}))
    when '!=' then return %(!Objects.equals(#{r}, #{args[0]}))
    when '!' then return %(!(#{r}))
    when 'nil?' then return %((#{r} == null))
    when 'empty?' then return %(isEmpty(#{r}))
    when 'size', 'length', 'count'
      return args.empty? ? %(size(#{r})) : %(count(#{r}, #{args.join ', '}))
    when 'include?' then return %(includes(#{r}, #{args[0]}))
    when 'key?', 'has_key?' then return %(#{r}.containsKey(#{map_key raw_args[0], args[0]}))
    when 'first' then return args.empty? ? %(first(#{r})) : %(first(#{r}, #{args[0]}))
    when 'last' then return args.empty? ? %(last(#{r})) : %(last(#{r}, #{args[0]}))
    when 'to_s' then return %(str(#{r}))
    when 'to_i' then return %(toInt(#{r}))
    when 'to_sym', 'intern' then return r
    when 'to_a' then return %(toList(#{r}))
    when 'keys' then return %(keys(#{r}))
    when 'values' then return %(values(#{r}))
    when 'start_with?' then return %(startsWith(#{r}, #{args.join ', '}))
    when 'end_with?' then return %(endsWith(#{r}, #{args.join ', '}))
    when 'join' then return %(join(#{r}#{args.empty? ? '' : ', ' + args[0]}))
    when 'split' then return %(split(#{r}#{args.map {|a| ', ' + a }.join}))
    when 'lines' then return %(lines(#{r}))
    when 'chomp' then return %(Strings.chomp(#{r}#{args.map {|a| ', ' + a }.join}))
    when 'strip' then return %(Strings.strip(#{r}))
    when 'rstrip' then return %(Strings.rstrip(#{r}))
    when 'lstrip' then return %(Strings.lstrip(#{r}))
    when 'chop' then return %(Strings.chop(#{r}))
    when 'squeeze' then return %(Strings.squeeze(#{r}#{args.map {|a| ', ' + a.sub(/\A"(.)"\z/, "'\\1'") }.join}))
    when 'tr' then return %(Strings.tr(#{r}, #{args.join ', '}))
    when 'capitalize' then return %(Strings.capitalize(#{r}))
    when 'succ', 'next' then return %(Strings.succ(#{r})) unless meth == 'next' && source_of(recv) =~ /term|node/
    when 'next' then return %(#{r}.getNext())
    when 'elements' then return %(#{r}.getElements())
    when 'to_f' then return %(Strings.toDouble(#{r}))
    when 'abs' then return %(Math.abs(#{r}))
    when 'ord' then return %(((int) #{r}.charAt(0)))
    when 'chr' then return %(Strings.chr(#{r}))
    when 'string' then return %(#{r}.getString())
    when 'encoding' then return %("UTF-8")
    when 'bytesize' then return %(#{r}.getBytes(java.nio.charset.StandardCharsets.UTF_8).length)
    when 'index' then return %(#{r}.indexOf(#{args.join ', '})) unless source_of(recv) =~ /section|sect|block/
    when 'rindex' then return %(#{r}.lastIndexOf(#{args.join ', '}))
    when 'downcase' then return %(#{r}.toLowerCase())
    when 'upcase' then return %(#{r}.toUpperCase())
    when 'dup', 'clone' then return %(copy(#{r}))
    when 'inspect' then return %(Strings.inspect(#{r}))
    when 'class' then return %(#{r}.getClass())
    when 'is_a?', 'kind_of?' then return %((#{r} instanceof #{args[0]}))
    when 'gsub' then return %(gsub(#{r}, #{args.join ', '}))
    when 'sub' then return %(sub(#{r}, #{args.join ', '}))
    when 'match?', '=~' then return %(matches(#{r}, #{args.join ', '}))
    when 'match' then return %(match(#{r}, #{args.join ', '}))
    when 'scan' then return %(scan(#{r}, #{args.join ', '}))
    when 'sort' then return %(sorted(#{r}))
    when 'uniq' then return %(uniq(#{r}))
    when 'compact' then return %(compact(#{r}))
    when 'flatten' then return %(flatten(#{r}))
    when 'reverse' then return %(reversed(#{r}))
    when 'freeze' then return r
    when 'delete' then return %(#{r}.remove(#{map_key raw_args[0], args[0]}))
    when 'merge' then return %(merge(#{r}, #{args.join ', '}))
    when 'push' then return %(#{r}.add(#{args.join ', '}))
    when '<<' then return append_expr(recv, r, args[0])
    when 'pop' then return %(pop(#{r}))
    when 'shift' then
      return %(shift(#{r})) unless reader_like?(recv)
    when 'slice' then return %(slice(#{r}, #{args.join ', '}))
    when 'call' then return %(#{r}.call(#{args.join ', '}))
    when 'map' then return %(/* FIXME map without block */ #{r})
    when 'attr'
      return attr_call(r, raw_args, args)
    when 'attr?'
      return attr_q_call(r, raw_args, args)
    when 'doctitle'
      return doctitle_call(r, raw_args, args)
    when 'catalog'
      return %(#{r}.getCatalog())
    when 'items'
      return %(((List) #{r}).getItems())
    when 'parse_header_metadata'
      return %(#{r}.parseHeaderMetadata(#{args.join ', '}))
    when 'read_lines_until'
      return %(#{r}.readLinesUntil(#{read_options raw_args}))
    when 'find_by', 'query'
      return %(#{r}.findBy(#{selector raw_args}))
    when 'process_method'
      return %(#{r}.getInstance())
    end
    jm = METHODS[meth] || default_method(meth, args)
    if meth.end_with?('=') && !%w(== != <= >=).include?(meth)
      return %(#{r}.#{jm}(#{args.join ', '}))
    end
    %(#{r}.#{jm}(#{args.join ', '}))
  end

  def reader_like? recv
    src = source_of(recv)
    src =~ /reader/
  end

  COMPLIANCE_BOOLEANS = %w(block_terminates_paragraph strict_verbatim_paragraphs underline_style_section_titles
    unwrap_standalone_preamble shorthand_property_syntax natural_xrefs markdown_syntax)

  def static_method cls, meth
    if cls == 'Compliance'
      return 'is' + camel(meth).then {|m| m[0].upcase + m[1..] } if COMPLIANCE_BOOLEANS.include? meth
      return 'get' + camel(meth).then {|m| m[0].upcase + m[1..] } unless meth == 'keys'
    end
    return 'hasExtname' if cls == 'Helpers' && meth == 'extname?'
    return 'getGroups' if cls == 'Extensions' && meth == 'groups'
    if cls == 'LoggerManager'
      return 'getLogger' if meth == 'logger'
      return 'setLogger' if meth == 'logger='
    end
    case meth
    when 'load' then 'load'
    when 'load_file' then 'loadFile'
    when 'convert', 'render' then 'convert'
    when 'convert_file', 'render_file' then 'convertFile'
    when 'for' then cls == 'SyntaxHighlighter' ? 'forName' : 'forBackend'
    else camel(meth.sub(/\Ais_(?=.+\?\z)/, '').sub(/\?\z/, '')).then {|m| meth.end_with?('?') ? 'is' + m[0].upcase + m[1..] : m }
    end
  end

  def default_method meth, args
    if meth.end_with? '?'
      base = camel meth.sub(/\Ais_/, '')
      %(is#{base[0].upcase}#{base[1..]})
    elsif meth.end_with? '='
      base = camel meth
      %(set#{base[0].upcase}#{base[1..]})
    elsif args.empty?
      base = camel meth
      %(get#{base[0].upcase}#{base[1..]})
    else
      camel meth
    end
  end

  def index_expr recv, r, raw_args, args
    a = raw_args[0]
    recv_src = source_of(recv)
    return %(slice(#{r}, #{args.join ', '})) if raw_args.size == 2
    if node?(a) && a.type == :LIT && Integer === a.children[0]
      i = a.children[0]
      if recv_src =~ /attributes|attrs|attr_hash|\bopts\b|options/ && recv_src !~ /blocks|items|lines|rows|columns|terms|attribute_entries|\[:\w+\]\z/
        return %(#{r}.get("#{i}"))
      end
      # the value of a Hash entry is not typed (e.g., options[:input_files][0])
      return %(get(#{r}, #{i})) if recv_src =~ /\[:\w+\]\z/ && r.start_with?('get(')
      return i < 0 ? (i == -1 ? %(last(#{r})) : %(#{r}.get(#{r}.size() - #{-i}))) : %(#{r}.get(#{i}))
    elsif node?(a) && a.type == :LIT && Symbol === a.children[0]
      sym = a.children[0].to_s
      return %(attributeEntries(#{r})) if sym == 'attribute_entries'
      if recv_src =~ /catalog|references/
        return %(#{r}.get#{camel(sym)[0].upcase}#{camel(sym)[1..]}())
      end
      return %(get(#{r}, "#{sym}"))
    elsif node?(a) && (a.type == :DOT2 || a.type == :DOT3)
      return %(slice(#{r}, #{args[0]}))
    elsif raw_args.size == 2
      return %(slice(#{r}, #{args.join ', '}))
    end
    %(get(#{r}, #{args[0]}))
  end

  def map_key raw, java
    if node?(raw) && raw.type == :LIT && Integer === raw.children[0]
      %("#{raw.children[0]}")
    elsif node?(raw) && raw.type == :LIT && raw.children[0] == :attribute_entries
      'Document.ATTRIBUTE_ENTRIES_KEY'
    else
      java
    end
  end

  def attr_call r, raw_args, args
    # positional attributes are stored with a String key
    args = args.each_with_index.map {|a, i| i == 0 && node?(raw_args[0]) && raw_args[0].type == :LIT && Integer === raw_args[0].children[0] ? %("#{a}") : a }
    case args.size
    when 1 then %(#{r}.getAttribute(#{args[0]}))
    when 2 then %(#{r}.getAttribute(#{args[0]}, #{args[1]}))
    else
      fallback = raw_args[2]
      fb = node?(fallback) && fallback.type == :TRUE ? args[0] : args[2]
      %(#{r}.getAttribute(#{args[0]}, #{args[1]}, #{fb}))
    end
  end

  def attr_q_call r, raw_args, args
    args = args.each_with_index.map {|a, i| i == 0 && node?(raw_args[0]) && raw_args[0].type == :LIT && Integer === raw_args[0].children[0] ? %("#{a}") : a }
    case args.size
    when 1 then %(#{r}.hasAttribute(#{args[0]}))
    when 2 then %(#{r}.isAttribute(#{args[0]}, #{args[1]}))
    else
      fallback = raw_args[2]
      fb = node?(fallback) && fallback.type == :TRUE ? args[0] : args[2]
      %(#{r}.hasAttribute(#{args[0]}, #{args[1]}, #{fb}))
    end
  end

  def doctitle_call r, raw_args, args
    opts = raw_args[0]
    return %(#{r}.getDoctitle()) unless opts
    keys = {}
    if node?(opts) && opts.type == :HASH
      list_items(opts.children[0]).each_slice(2) {|k, v| keys[k.children[0].to_s] = v }
    end
    if keys['partition']
      sep = keys['partition']
      sep_java = node?(sep) && sep.type == :TRUE ? 'null' : expr(sep)
      %(#{r}.getPartitionedDoctitle(#{sep_java}, #{keys['sanitize'] ? 'true' : 'false'}, #{keys['use_fallback'] ? 'true' : 'false'}))
    else
      %(#{r}.getDoctitle(#{keys['sanitize'] ? expr(keys['sanitize']) : 'false'}, #{keys['use_fallback'] ? expr(keys['use_fallback']) : 'false'}))
    end
  end

  def read_options raw_args
    opts = raw_args[0]
    return 'Reader.ReadOptions.create()' unless opts && node?(opts) && opts.type == :HASH
    chain = ['Reader.ReadOptions.create()']
    list_items(opts.children[0]).each_slice(2) do |k, v|
      key = k.children[0].to_s
      case key
      when 'terminator' then chain << %(.terminator(#{expr v}))
      when 'context' then chain << %(.context(#{expr v}))
      when 'cursor' then chain << (node?(v) && v.type == :LIT ? '.cursorAtMark()' : %(.cursor(#{expr v})))
      else
        chain << (node?(v) && v.type == :TRUE ? %(.#{camel key}()) : %(.#{camel key}(#{expr v})))
      end
    end
    chain.join
  end

  def selector raw_args
    opts = raw_args[0]
    return 'AbstractBlock.Selector.any()' unless opts && node?(opts) && opts.type == :HASH
    chain = ['AbstractBlock.Selector.any()']
    list_items(opts.children[0]).each_slice(2) do |k, v|
      key = k.children[0].to_s
      chain << %(.with#{camel(key)[0].upcase}#{camel(key)[1..]}(#{expr v}))
    end
    chain.join
  end

  def append_expr recv_node, recv, arg
    src = source_of(recv_node)
    if src =~ /\A(doc|document|section|sect\w*|block|parent|preamble|para\w*|item|list_item|olist|ulist|dlist|colist|table|open|example|sidebar|outer|inner)\b/ && src !~ /\.(lines|blocks|items|attributes)\z/
      %(#{recv}.append(#{arg}))
    else
      %(#{recv}.add(#{arg}))
    end
  end

  def opcall n
    recv = expr n.children[0]
    op = n.children[1].to_s
    arg_nodes = list_items(n.children[2])
    arg = arg_nodes[0] ? expr(arg_nodes[0]) : nil
    case op
    when '==' then %(Objects.equals(#{recv}, #{arg}))
    when '!=' then %(!Objects.equals(#{recv}, #{arg}))
    when '!' then %(!(#{recv}))
    when '=~' then %(matches(#{recv}, #{arg}))
    when '!~' then %(!matches(#{recv}, #{arg}))
    when '<<' then append_expr(n.children[0], recv, arg)
    when '*'
      lhs = n.children[0]
      if node?(lhs) && (lhs.type == :STR || lhs.type == :DSTR || (lhs.type == :CONST && (@string_constants || []).include?(lhs.children[0])))
        %(#{recv}.repeat(#{arg}))
      elsif node?(lhs) && lhs.type == :LIST
        %(multiply(#{recv}, #{arg}))
      elsif node?(arg_nodes[0]) && %i(STR DSTR).include?(arg_nodes[0].type) || arg =~ /\AAsciidoctor\.LF\z/
        # Array#* with a String argument is an alias of Array#join
        %(join(#{recv}, #{arg}))
      else
        %((#{recv} * #{arg}))
      end
    when '+'
      lhs = n.children[0]
      if node?(lhs) && (lhs.type == :LIST || lhs.type == :ZLIST) || (arg_nodes[0] && node?(arg_nodes[0]) && %i(LIST ZLIST).include?(arg_nodes[0].type))
        %(plus(#{recv}, #{arg}))
      else
        %((#{recv} + #{arg}))
      end
    when '-@' then %(-#{recv})
    when '[]' then index_expr(n.children[0], recv, arg_nodes, arg_nodes.map {|a| expr a })
    else %((#{recv} #{op} #{arg}))
    end
  end

  def attrasgn n
    attrasgn_with n, nil
  end

  # Translates an attribute assignment (e.g., obj.name = value), optionally using the specified value expression.
  def attrasgn_with n, value
    recv = n.children[0]
    meth = n.children[1].to_s
    args = list_items(n.children[2])
    r = expr recv
    if meth == '[]='
      return %(#{r}.put(#{map_key args[0], expr(args[0])}, #{value || expr(args[1])}))
    end
    cname = const_name(recv)
    if cname && java_const(cname) == 'Compliance'
      return %(Compliance.set#{camel(meth)[0].upcase}#{camel(meth)[1..]}(#{value || expr(args[0])}))
    end
    if cname && java_const(cname) == 'LoggerManager' && meth == 'logger='
      return %(LoggerManager.setLogger(#{value || expr(args[0])}))
    end
    jm = METHODS[meth] || default_method(meth, [1])
    %(#{r}.#{jm}(#{value || args.map {|a| expr a }.join(', ')}))
  end

  def iter_expr n
    if (dsl = dsl_iter n)
      return dsl
    end
    call = n.children[0]
    params = iter_params n
    body = iter_body n
    ps = params.map {|p| camel p }
    lambda_params = ps.size == 1 ? ps[0] : %((#{ps.join ', '}))
    lambda_body = lambda_body_of(params, body)
    if call.type == :CALL
      recv = expr call.children[0]
      meth = call.children[1].to_s
      args = call_args(call).map {|a| expr a }
      case meth
      when 'map', 'collect' then return %(map(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'each_with_object'
        return %(eachWithObject(#{recv}, #{args[0]}, #{lambda_params} -> #{lambda_body}))
      when 'select', 'filter' then return %(select(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'reject' then return %(reject(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'find', 'detect' then return %(find(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'count' then return %(count(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'any?' then return %(any(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'all?' then return %(all(#{recv}, #{lambda_params} -> #{lambda_body}))
      when 'find_by', 'query'
        # the filter returns a Ruby-like value (e.g., true, false, :prune), which is converted to a FilterVerdict
        return %(#{recv}.findBy(#{selector call_args(call)}, filter(#{ps.empty? ? 'it' : lambda_params} -> #{lambda_body})))
      when 'read_lines_until'
        return %(#{recv}.readLinesUntil(#{read_options call_args(call)}, #{lambda_params} -> #{lambda_body}))
      when 'tap' then return %(tap(#{recv}, #{lambda_params} -> #{lambda_body}))
      end
      cname = const_name(call.children[0])
      jrecv = cname ? java_const(cname) : recv
      return %(#{jrecv}.#{METHODS[meth] || camel(meth)}(#{(args + ["#{lambda_params} -> #{lambda_body}"]).join ', '}))
    elsif call.type == :FCALL
      name = call.children[0].to_s
      args = call_args(call).map {|a| expr a }
      case name
      when 'lambda', 'proc' then return %(#{lambda_params} -> #{lambda_body})
      when 'assert_raises'
        return %(assertThrows(#{args.map {|a| class_literal a }.join ', '}, () -> #{lambda_body}))
      when 'using_test_webserver'
        return %(usingTestWebserver(() -> #{lambda_body}))
      when 'run_command'
        return %(runCommand(list(#{args.join ', '}), #{lambda_params} -> #{lambda_body}))
      when 'using_memory_logger'
        return %(/* FIXME using_memory_logger expression */ usingMemoryLogger(#{args.join ', '}))
      end
      return %(#{camel helper_name(name)}(#{(args + ["#{lambda_params} -> #{lambda_body}"]).join ', '}))
    end
    %(/* FIXME iter */ #{java_string source_of(n)})
  end

  def lambda_expr n
    scope = n.children[0]
    params = scope.children[0].first(arity_of(scope))
    ps = params.map {|p| camel p }
    lambda_params = ps.size == 1 ? ps[0] : %((#{ps.join ', '}))
    %(#{lambda_params} -> #{lambda_body_of(params, scope.children[2])})
  end

  # --- extension DSL (blocks that are evaluated with instance_eval in Ruby) ---

  # the processor kinds (and the arity of their process function) that can be registered using a block
  DSL_PROCESSOR_KINDS = {
    'preprocessor' => ['preprocessor', 2], 'tree_processor' => ['treeProcessor', 1], 'treeprocessor' => ['treeProcessor', 1],
    'postprocessor' => ['postprocessor', 2], 'include_processor' => ['includeProcessor', 4],
    'docinfo_processor' => ['docinfoProcessor', 1], 'block' => ['block', 3], 'block_macro' => ['blockMacro', 3],
    'inline_macro' => ['inlineMacro', 3]
  }

  REGISTRY_DSL_METHODS = (DSL_PROCESSOR_KINDS.keys + %w(document groups prefer)).to_set

  PROCESSOR_DSL_METHODS = %w(named on_context on_contexts contexts bind_to parse_content_as content_model
    positional_attributes positional_attrs name_positional_attributes name_attributes pos_attrs default_attributes
    default_attrs resolve_attributes resolves_attributes match_format using_format format match process handles?
    at_location prefer option config create_section create_block create_list create_list_item create_image_block
    create_inline create_anchor create_inline_pass create_paragraph create_open_block create_example_block
    create_pass_block create_listing_block create_literal_block parse_content parse_attributes).to_set

  DSL_METHOD_NAMES = {
    'positional_attrs' => 'positionalAttributes', 'name_positional_attributes' => 'positionalAttributes',
    'name_attributes' => 'positionalAttributes', 'pos_attrs' => 'positionalAttributes',
    'default_attrs' => 'defaultAttributes', 'resolves_attributes' => 'resolveAttributes', 'handles?' => 'handles',
    'on_contexts' => 'onContexts', 'document' => 'getDocument', 'groups' => 'getGroups', 'config' => 'getConfig'
  }

  def dsl_frames
    @dsl_frames ||= []
  end

  # Returns the frame (kind, variable, processor kind) of the DSL receiver of the method, if any.
  def dsl_frame_for name
    dsl_frames.reverse_each do |frame|
      return frame if frame[0] == :processor && PROCESSOR_DSL_METHODS.include?(name)
      return frame if frame[0] == :registry && REGISTRY_DSL_METHODS.include?(name)
    end
    nil
  end

  def dsl_method name
    DSL_METHOD_NAMES[name] || camel(name)
  end

  # Translates the arguments of a DSL method (e.g., a content model symbol).
  def dsl_args name, raw_args
    raw_args.map do |a|
      if %w(parse_content_as content_model).include?(name) && node?(a) && a.type == :LIT && Symbol === a.children[0]
        %(ContentModel.#{a.children[0].to_s.upcase})
      else
        expr a
      end
    end
  end

  def with_dsl_frame kind, var, processor_kind = nil
    dsl_frames.push [kind, var, processor_kind]
    yield
  ensure
    dsl_frames.pop
  end

  # Translates a DSL block (e.g., Extensions.register do ... end, block_macro do ... end, process do ... end), or
  # returns nil if the block is not a DSL block.
  def dsl_iter n
    call = n.children[0]
    name = call.type == :FCALL ? call.children[0].to_s : (call.type == :CALL ? call.children[1].to_s : nil)
    return unless name
    params = iter_params n
    body = iter_body n
    if call.type == :CALL && %w(register create).include?(name) && const_name(call.children[0]) =~ /\A(Asciidoctor::)?Extensions\z/
      var = params[0] ? camel(params[0]) : 'registry'
      # NOTE the lambda parameter cannot shadow a local variable (e.g., registry = Extensions.create do ... end)
      var = 'reg' if declared?(var.to_sym)
      args = call_args(call).map {|a| expr a }
      inner = dsl_lambda_body(params[0] ? params : [var.to_sym], body) { with_dsl_frame(:registry, var) { yield_body body } }
      return %(Extensions.#{name}(#{(args + ["(Group) #{var} -> #{inner}"]).join ', '}))
    end
    frame = call.type == :FCALL ? dsl_frame_for(name) : nil
    return unless frame
    if frame[0] == :registry && DSL_PROCESSOR_KINDS.key?(name)
      java_name, _ = DSL_PROCESSOR_KINDS[name]
      var = params[0] ? camel(params[0]) : 'processor'
      args = dsl_args name, call_args(call)
      inner = dsl_lambda_body([var.to_sym], body) { with_dsl_frame(:processor, var, name) { yield_body body } }
      return %(#{frame[1]}.#{java_name}(#{(args + ["#{var} -> #{inner}"]).join ', '}))
    elsif frame[0] == :processor && %w(process handles?).include?(name)
      arity = name == 'process' ? DSL_PROCESSOR_KINDS[frame[2]][1] : 1
      ps = params.map {|p| camel p }
      ps << 'ignored' while ps.size < arity
      ps = unique_params ps
      lambda_params = ps.size == 1 ? ps[0] : %((#{ps.join ', '}))
      if name == 'process' && frame[2] == 'include_processor'
        # the process method of an include processor doesn't return a value
        inner = dsl_lambda_body(params, body) { yield_body body }
        return %(#{frame[1]}.process(#{lambda_params} -> #{inner}))
      end
      return %(#{frame[1]}.#{dsl_method name}(#{lambda_params} -> #{lambda_body_of params, body}))
    elsif frame[0] == :registry && name == 'prefer'
      # prefer :tree_processor do ... end
      kind_node = call_args(call)[0]
      kind = kind_node.children[0].to_s
      java_name, _ = DSL_PROCESSOR_KINDS[kind]
      var = params[0] ? camel(params[0]) : 'processor'
      inner = dsl_lambda_body([var.to_sym], body) { with_dsl_frame(:processor, var, kind) { yield_body body } }
      return %(#{frame[1]}.prefer(#{frame[1]}.#{java_name}(#{var} -> #{inner})))
    end
    nil
  end

  def yield_body body
    translate_statements body, false
  end

  # Translates the body of a DSL block (a lambda whose value is ignored).
  def dsl_lambda_body params, body
    saved_out = @out
    saved_indent = @indent
    @out = []
    @indent = saved_indent + 1
    within_block(:lambda) { with_scope(params) { yield } }
    inner = @out
    @out = saved_out
    @indent = saved_indent
    %({\n#{inner.join "\n"}\n#{'    ' * saved_indent}})
  end

  def lambda_body_of params, body
    stmts = block_statements body
    if stmts.size == 1 && expression_node?(stmts[0]) && stmts[0].type != :ITER && stmts[0].type != :ATTRASGN
      java = within_block(:lambda) { with_scope(params) { expr(stmts[0]) } }
      # a throw statement cannot be used as the body of an expression lambda
      return java.start_with?('throw ', 'return ') ? %({ #{java}; }) : java
    end
    saved_out = @out
    saved_indent = @indent
    @out = []
    @indent = saved_indent + 1
    within_block(:lambda) { with_scope(params) { translate_statements body, true } }
    inner = @out
    @out = saved_out
    @indent = saved_indent
    %({\n#{inner.join "\n"}\n#{'    ' * saved_indent}})
  end
end

if $PROGRAM_NAME == __FILE__
  file = ARGV[0]
  class_name = ARGV[1] || File.basename(file, '.rb').split('_').map(&:capitalize).join
  puts Rb2Java.new(File.read(file), class_name).translate
end
