# IMPORTANT: Before releasing this package, copy/paste the next 2 lines into PowerShell to remove all comments from this file:
#   $f='c:\path\to\thisFile.ps1'
#   gc $f | ? {$_ -notmatch "^\s*#"} | % {$_ -replace '(^.*?)\s*?[^``]#.*','$1'} | Out-File $f+".~" -en utf8; mv -fo $f+".~" $f

$ErrorActionPreference = 'Stop'; # stop on all errors

$adocjVersion = '2.1.0'
$packageName= 'asciidoctorj' # arbitrary name for the package, used in messages
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
# Which is the preferred mirror? There are many listed at
# http://www.apache.org/dyn/closer.cgi/xmlgraphics/fop
$url        = "https://dl.bintray.com/asciidoctor/maven/org/asciidoctor/asciidoctorj/$($adocjVersion)/asciidoctorj-$($adocjVersion)-bin.zip" # download url
#$url64      = '' # 64bit URL here or remove - if installer is both, use $url
#$fileLocation = Join-Path $toolsDir 'NAME_OF_EMBEDDED_INSTALLER_FILE'
#$fileLocation = Join-Path $toolsDir 'SHARE_LOCATION_OF_INSTALLER_FILE'

$packageArgs = @{
  packageName   = $packageName
  unzipLocation = $toolsDir
  url           = $url
  checksum      = '708bb954d09eeb862ed7627e743be8f96f2a67397c561ab56e85b6bc4650cc77'
  checksumType  = 'sha256' #default is md5, can also be sha1
  #checksum64    = ''
  #checksumType64= 'md5' #default is checksumType
}

## Main helper functions - these have error handling tucked into them already
## see https://github.com/chocolatey/choco/wiki/HelpersReference

Install-ChocolateyZipPackage @packageArgs

Install-BinFile -name 'asciidoctorj' -path "$toolsDir\asciidoctorj-$($adocjVersion)\bin\asciidoctorj.bat"
