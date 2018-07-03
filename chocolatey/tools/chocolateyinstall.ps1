# IMPORTANT: Before releasing this package, copy/paste the next 2 lines into PowerShell to remove all comments from this file:
#   $f='c:\path\to\thisFile.ps1'
#   gc $f | ? {$_ -notmatch "^\s*#"} | % {$_ -replace '(^.*?)\s*?[^``]#.*','$1'} | Out-File $f+".~" -en utf8; mv -fo $f+".~" $f

$ErrorActionPreference = 'Stop'; # stop on all errors

$adocjVersion = '1.5.7'
$packageName= 'asciidoctorj' # arbitrary name for the package, used in messages
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$url        = "https://dl.bintray.com/asciidoctor/maven/org/asciidoctor/asciidoctorj/$($adocjVersion)/asciidoctorj-$($adocjVersion)-bin.zip" # download url

$packageArgs = @{
  packageName   = $packageName
  unzipLocation = $toolsDir
  url           = $url
  # curl -I https://dl.bintray.com/asciidoctor/maven/org/asciidoctor/asciidoctorj/1.5.7/asciidoctorj-1.5.7-bin.zip
  checksum      = 'b0295bb73589f389a6b62563e2fd018b0aa6095feacb5acfa7f534b1265e67d1'
  checksumType  = 'sha256'
}

## Main helper functions - these have error handling tucked into them already
## see https://github.com/chocolatey/choco/wiki/HelpersReference

Install-ChocolateyZipPackage @packageArgs

Install-BinFile -name 'asciidoctorj' -path "$toolsDir\asciidoctorj-$($adocjVersion)\bin\asciidoctorj.bat"
