# Usage: ruby -I <asciidoctor>/lib convert_ruby.rb <list-file> <root> <out-dir>
# Converts each file of the list (paths relative to root) to <out-dir>/<path>.html (+ .log).
require 'asciidoctor'
require 'fileutils'

list, root, out = ARGV
# extra attributes, e.g. ATTRS='toc sectnums icons=font'
extra = (ENV['ATTRS'] || '').split.to_h {|a| k, v = a.split '=', 2; [k, v || ''] }
File.readlines(list, chomp: true).reject(&:empty?).each do |rel|
  src = File.join root, rel
  dst = File.join out, rel
  FileUtils.mkdir_p File.dirname dst
  logger = Asciidoctor::MemoryLogger.new
  Asciidoctor::LoggerManager.logger = logger
  begin
    html = Asciidoctor.convert_file src, safe: :safe, standalone: true, to_file: false,
      attributes: { 'reproducible' => '', 'linkcss' => '', 'docdate' => '2020-01-01', 'doctime' => '00:00:00 UTC',
                    'localdate' => '2020-01-01', 'localtime' => '00:00:00 UTC', 'docyear' => '2020', 'localyear' => '2020' }.merge(extra)
  rescue Exception => e
    html = %(EXCEPTION: #{e.class}: #{e.message}\n)
  end
  File.write %(#{dst}.html), html.to_s
  File.write %(#{dst}.log), logger.messages.map {|m|
    msg = m[:message]
    if Hash === msg
      loc = msg[:source_location]
      %(#{m[:severity]}: #{loc ? "line #{loc.lineno}: " : ''}#{msg[:text]})
    else
      %(#{m[:severity]}: #{msg})
    end
  }.join("\n")
end
