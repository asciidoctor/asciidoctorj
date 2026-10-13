# IMPORTANT: Before releasing this package, copy/paste the next 2 lines into PowerShell to remove all comments from this file:
#   $f='c:\path\to\thisFile.ps1'
#   gc $f | ? {$_ -notmatch "^\s*#"} | % {$_ -replace '(^.*?)\s*?[^``]#.*','$1'} | Out-File $f+".~" -en utf8; mv -fo $f+".~" $f

$ErrorActionPreference = 'Stop'; # stop on all errors

$adocjVersion = '1.0.0-rc.1'
$packageName  = 'asciidoctorj'
$toolsDir     = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
# the native executable for Windows published on the GitHub release
$url          = "https://github.com/yuzutech/asciidoctorj/releases/download/v$($adocjVersion)/asciidoctorj-$($adocjVersion)-windows-amd64.zip"

$packageArgs = @{
  packageName   = $packageName
  unzipLocation = $toolsDir
  url64bit      = $url
  # the checksum of the archive is listed in the SHA256SUMS file of the GitHub release
  checksum64    = 'f9261a6e0f605bae5dc0f37c3050b090edccf411b7eca8c8cf758379e255e11b'
  checksumType64 = 'sha256'
}

## Main helper functions - these have error handling tucked into them already
## see https://github.com/chocolatey/choco/wiki/HelpersReference

# NOTE Chocolatey automatically creates a shim for asciidoctorj.exe (and removes it on uninstall)
Install-ChocolateyZipPackage @packageArgs
