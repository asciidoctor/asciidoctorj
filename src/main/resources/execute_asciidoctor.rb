require 'bundler/setup'
require 'asciidoctor'

Dir.glob("#{$srcDir}/**/*.a*").each do |path|
  if path =~ /.*\.a((sc(iidoc)?)|d(oc)?)$/
    Asciidoctor.render_file(path, {:in_place => false, :safe => Asciidoctor::SafeMode::UNSAFE,
                                   :attributes => {'backend' => $backend}, :to_dir => $outputDir})
  end
end
