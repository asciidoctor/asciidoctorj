# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = "asciidoctor"
  s.version = "0.1.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Ryan Waldron", "Dan Allen", "Jeremy McAnally"]
  s.date = "2013-02-26"
  s.description = "A pure Ruby processor to turn Asciidoc-formatted documents into HTML (and, eventually, other formats perhaps)."
  s.email = "rew@erebor.com"
  s.executables = ["asciidoctor"]
  s.extra_rdoc_files = ["LICENSE"]
  s.files = ["bin/asciidoctor", "LICENSE"]
  s.homepage = "http://github.com/asciidoctor"
  s.rdoc_options = ["--charset=UTF-8"]
  s.require_paths = ["lib"]
  s.rubyforge_project = "asciidoctor"
  s.rubygems_version = "1.8.24"
  s.summary = "Pure Ruby Asciidoc to HTML rendering."

  if s.respond_to? :specification_version then
    s.specification_version = 2

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<coderay>, [">= 0"])
      s.add_development_dependency(%q<erubis>, [">= 0"])
      s.add_development_dependency(%q<htmlentities>, [">= 0"])
      s.add_development_dependency(%q<mocha>, [">= 0"])
      s.add_development_dependency(%q<nokogiri>, [">= 0"])
      s.add_development_dependency(%q<pending>, [">= 0"])
      s.add_development_dependency(%q<rake>, [">= 0"])
      s.add_development_dependency(%q<rdoc>, ["~> 3.12"])
      s.add_development_dependency(%q<tilt>, [">= 0"])
    else
      s.add_dependency(%q<coderay>, [">= 0"])
      s.add_dependency(%q<erubis>, [">= 0"])
      s.add_dependency(%q<htmlentities>, [">= 0"])
      s.add_dependency(%q<mocha>, [">= 0"])
      s.add_dependency(%q<nokogiri>, [">= 0"])
      s.add_dependency(%q<pending>, [">= 0"])
      s.add_dependency(%q<rake>, [">= 0"])
      s.add_dependency(%q<rdoc>, ["~> 3.12"])
      s.add_dependency(%q<tilt>, [">= 0"])
    end
  else
    s.add_dependency(%q<coderay>, [">= 0"])
    s.add_dependency(%q<erubis>, [">= 0"])
    s.add_dependency(%q<htmlentities>, [">= 0"])
    s.add_dependency(%q<mocha>, [">= 0"])
    s.add_dependency(%q<nokogiri>, [">= 0"])
    s.add_dependency(%q<pending>, [">= 0"])
    s.add_dependency(%q<rake>, [">= 0"])
    s.add_dependency(%q<rdoc>, ["~> 3.12"])
    s.add_dependency(%q<tilt>, [">= 0"])
  end
end
