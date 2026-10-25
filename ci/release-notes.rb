# Extracts the section of a version from CHANGELOG.adoc and converts it to Markdown,
# so that it can be used as the notes of a GitHub release.
#
# Usage: ruby ci/release-notes.rb VERSION [CHANGELOG]

version = ARGV[0] or abort 'Usage: ruby ci/release-notes.rb VERSION [CHANGELOG]'
changelog = ARGV[1] || File.join(__dir__, '..', 'CHANGELOG.adoc')
lines = File.readlines(changelog, chomp: true)

# Attributes defined in the document header, e.g. :url-repo: https://github.com/asciidoctor/asciidoctorj
attributes = {}
lines.each do |line|
  break if line.start_with?('== ')
  attributes[$1] = $2 if line =~ /^:([\w-]+):\s+(.*)$/
end
resolve = ->(text) { text.gsub(/\{([\w-]+)\}/) { attributes.key?($1) ? resolve.(attributes[$1]) : $& } }

start = lines.index { |line| line =~ /^== #{Regexp.escape(version)}(\s|$)/ } or abort "No section for #{version} in #{changelog}"
finish = lines[(start + 1)..].index { |line| line.start_with?('== ') }
section = finish ? lines[(start + 1)..(start + finish)] : lines[(start + 1)..]

notes = section.map do |line|
  line = resolve.(line)
  line = line.gsub(/(?:link:)?(https?:\/\/[^\s\[]+)\[([^\]]*)\]/) { $2.empty? ? $1 : "[#{$2}](#{$1})" }
  case line
  when /^(=+) (.*)$/ then "#{'#' * ($1.length + 1)} #{$2}"
  when /^(\S.*)::$/ then "**#{$1}**"
  else line
  end
end

puts notes.join("\n").strip
