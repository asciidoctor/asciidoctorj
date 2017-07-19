module AsciidoctorJ
    include_package 'org.asciidoctor'
    module Extensions
        include_package 'org.asciidoctor.extension'
        # Treeprocessor was renamed in to TreeProcessor in https://github.com/asciidoctor/asciidoctor/commit/f1dd816ade9db457b899581841e4cf7b788aa26d
        # This is necessary to run against both Asciidoctor 1.5.5 and 1.5.6
        TreeProcessor = Treeprocessor unless defined? TreeProcessor
    end
end

require 'java'
require 'asciidoctor'
require 'asciidoctor/extensions'

class AsciidoctorModule
    java_implements Java::Asciidoctor

    def unregister_all_extensions()
        Asciidoctor::Extensions.unregister_all
    end

    def unregister_extension name
        Asciidoctor::Extensions.unregister name
    end

    def docinfo_processor(extensionName, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            docinfo_processor extensionName
        end
    end

    def treeprocessor(extensionName, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            treeprocessor extensionName
        end
    end
    
    def include_processor(extensionName, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            include_processor extensionName
        end
    end

    def preprocessor(extensionName, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            preprocessor extensionName
        end
    end
    
    def postprocessor(extensionName, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            postprocessor extensionName
        end
    end

    def block_processor(extensionName, blockSymbol, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            block extensionName, blockSymbol
        end
    end

    def block_macro(extensionName, blockSymbol, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            block_macro extensionName, blockSymbol
        end
    end

    def inline_macro(extensionName, blockSymbol, groupName = nil)
        Asciidoctor::Extensions.register groupName do
            inline_macro extensionName, blockSymbol
        end
    end

    def register_extension_group(extensionGroupImpl)
        Asciidoctor::Extensions.register extensionGroupImpl.groupName do
            extensionGroupImpl.docinfoProcessors.each {|p|
                docinfo_processor p
            }
            extensionGroupImpl.preprocessors.each {|p|
                preprocessor p
            }
            extensionGroupImpl.postprocessors.each {|p|
                postprocessor p
            }
            extensionGroupImpl.includeProcessors.each {|p|
                include_processor p
            }
            extensionGroupImpl.treeProcessors.each {|p|
                tree_processor p
            }
            extensionGroupImpl.blockprocessors.entrySet().each {|p|
                block p.value, p.key.to_sym
            }
            extensionGroupImpl.blockMacros.entrySet().each {|p|
                block_macro p.value, p.key.to_sym
            }
            extensionGroupImpl.inlineMacros.entrySet().each {|p|
                inline_macro p.value, p.key.to_sym
            }
        end
    end

    def convert_file(filename, options = {})
        return Asciidoctor.convert_file(filename, options)
    end

    def convert(content, options = {})
        return Asciidoctor.convert(content, options)
    end

    def load_file(filename, options = {})
        return Asciidoctor.load_file(filename, options)
    end

    def load(content, options = {})
        return Asciidoctor.load(content, options)
    end

    def register_converter(converter, backends = ['*'])
        Asciidoctor::Converter::Factory.register converter, backends
    end

    def resolve_converter(backend)
        return Asciidoctor::Converter::Factory.resolve backend
    end

    def converters
        return Asciidoctor::Converter::Factory.converters.keys
    end

    def unregister_all_converters
        Asciidoctor::Converter::Factory.unregister_all
    end

    def asciidoctorRuntimeEnvironmentVersion
        return Asciidoctor::VERSION
    end
end

module Asciidoctor
    class AbstractNode
        alias :is_attr :attr? unless method_defined? :is_attr
        alias :get_attr :attr unless method_defined? :get_attr
        alias :is_reftext :reftext? unless method_defined? :is_reftext
    end
    
    class AbstractBlock
        alias :append :<< unless method_defined? :append
    end
end
