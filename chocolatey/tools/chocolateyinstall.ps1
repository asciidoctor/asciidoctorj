# IMPORTANT: Before releasing this package, copy/paste the next 2 lines into PowerShell to remove all comments from this file:
#   $f='c:\path\to\thisFile.ps1'
#   gc $f | ? {$_ -notmatch "^\s*#"} | % {$_ -replace '(^.*?)\s*?[^``]#.*','$1'} | Out-File $f+".~" -en utf8; mv -fo $f+".~" $f

$ErrorActionPreference = 'Stop'; # stop on all errors

$adocjVersion = '2.5.3'
$packageName  = 'asciidoctorj' # arbitrary name for the package, used in messages
$toolsDir     = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$url          = "https://search.maven.org/remotecontent?filepath=org/asciidoctor/asciidoctorj/$($adocjVersion)/asciidoctorj-$($adocjVersion)-bin.zip"
#$url64      = '' # 64bit URL here or remove - if installer is both, use $url
#$fileLocation = Join-Path $toolsDir 'NAME_OF_EMBEDDED_INSTALLER_FILE'
#$fileLocation = Join-Path $toolsDir 'SHARE_LOCATION_OF_INSTALLER_FILE'

$packageArgs = @{
  packageName   = $packageName
  unzipLocation = $toolsDir
  url           = $url
  checksum      = '2af46345b0fc56fe9936a719f29185d12e89b8f0f2f85a3423da2e6a9448ded0'
  checksumType  = 'sha256'
  #checksum64    = ''
  #checksumType64= 'md5' #default is checksumType
}

## Main helper functions - these have error handling tucked into them already
## see https://github.com/chocolatey/choco/wiki/HelpersReference

Install-ChocolateyZipPackage @packageArgs

# XXX Check for java.exe

Install-BinFile -name 'asciidoctorj' -path "$toolsDir\asciidoctorj-$($adocjVersion)\bin\asciidoctorj.bat"
