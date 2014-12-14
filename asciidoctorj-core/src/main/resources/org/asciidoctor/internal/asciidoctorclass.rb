module AsciidoctorJ
    include_package 'org.asciidoctor'
    module Extensions
        include_package 'org.asciidoctor.extension'
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

    def docinfo_processor(extensionName)
        Asciidoctor::Extensions.register do
            docinfo_processor extensionName
        end
    end

    def treeprocessor(extensionName)
        Asciidoctor::Extensions.register do 
            treeprocessor extensionName
        end
    end
    
    def include_processor(extensionName)
        Asciidoctor::Extensions.register do
            include_processor extensionName
        end
    end

    def preprocessor(extensionName)
        Asciidoctor::Extensions.register do
            preprocessor extensionName
        end
    end
    
    def postprocessor(extensionName)
        Asciidoctor::Extensions.register do
            postprocessor extensionName
        end
    end

    def block_processor(extensionName, blockSymbol)
        Asciidoctor::Extensions.register do
            block extensionName, blockSymbol
        end
    end

    def block_macro(extensionName, blockSymbol)
        Asciidoctor::Extensions.register do
            block_macro extensionName, blockSymbol
        end
    end

    def inline_macro(extensionName, blockSymbol)
        Asciidoctor::Extensions.register do
            inline_macro extensionName, blockSymbol
        end
    end

    def convert_file(content, options = {})
        return Asciidoctor.convert_file(content, options)
    end

    def convert(content, options = {})
        return Asciidoctor.convert(content, options)
    end

    def load_file(content, options = {})
        return Asciidoctor.load_file(content, options)
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

    def converters()
        return Asciidoctor::Converter::Factory.converters.keys
    end

    def unregister_all_converters
        Asciidoctor::Converter::Factory.unregister_all
    end

    def asciidoctorRuntimeEnvironmentVersion()
        return Asciidoctor::VERSION
    end
end