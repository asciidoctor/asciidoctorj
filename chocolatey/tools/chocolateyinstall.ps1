# IMPORTANT: Before releasing this package, copy/paste the next 2 lines into PowerShell to remove all comments from this file:
#   $f='c:\path\to\thisFile.ps1'
#   gc $f | ? {$_ -notmatch "^\s*#"} | % {$_ -replace '(^.*?)\s*?[^``]#.*','$1'} | Out-File $f+".~" -en utf8; mv -fo $f+".~" $f

$ErrorActionPreference = 'Stop'; # stop on all errors

$adocjVersion = '2.5.7'
$packageName  = 'asciidoctorj'
$toolsDir     = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$url          = "https://search.maven.org/remotecontent?filepath=org/asciidoctor/asciidoctorj/$($adocjVersion)/asciidoctorj-$($adocjVersion)-bin.zip"

$packageArgs = @{
  packageName   = $packageName
  unzipLocation = $toolsDir
  url           = $url
  checksum      = 'e25292735620cbb31af11e2fc730b52d9dac35a68f2f2f7ca4ee5aef0dfb7c37'
  checksumType  = 'sha256'
}

## Main helper functions - these have error handling tucked into them already
## see https://github.com/chocolatey/choco/wiki/HelpersReference

Install-ChocolateyZipPackage @packageArgs

# XXX Check for java.exe

Install-BinFile -name 'asciidoctorj' -path "$toolsDir\asciidoctorj-$($adocjVersion)\bin\asciidoctorj.bat"
