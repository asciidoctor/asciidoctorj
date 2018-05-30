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

module AsciidoctorModule

    def self.unregister_all_extensions()
        Asciidoctor::Extensions.unregister_all
    end

    def self.unregister_extension name
        Asciidoctor::Extensions.unregister name
    end

    def self.docinfo_processor(extensionName)
        Asciidoctor::Extensions.register do
            docinfo_processor extensionName
        end
    end

    def self.treeprocessor(extensionName)
        Asciidoctor::Extensions.register do
            treeprocessor extensionName
        end
    end
    
    def self.include_processor(extensionName)
        Asciidoctor::Extensions.register do
            include_processor extensionName
        end
    end

    def self.preprocessor(extensionName)
        Asciidoctor::Extensions.register do
            preprocessor extensionName
        end
    end
    
    def self.postprocessor(extensionName)
        Asciidoctor::Extensions.register do
            postprocessor extensionName
        end
    end

    def self.block_processor *args
        Asciidoctor::Extensions.register do
            block *args
        end
    end

    def self.block_macro *args
        Asciidoctor::Extensions.register do
            block_macro *args
        end
    end

    def self.inline_macro *args
        Asciidoctor::Extensions.register do
            inline_macro *args
        end
    end

    def self.register_extension_group(groupName, callback, registrators)
        Asciidoctor::Extensions.register groupName do
            callback.register_extensions self, registrators
        end
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
