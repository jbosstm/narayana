$DL_SERVER="http://kent.dl.sourceforge.net/project/gnuwin32"
$LIBINTL_URL="$DL_SERVER/libintl/0.14.4/libintl-0.14.4-bin.zip"
$LIBICONV_URL="$DL_SERVER/libiconv/1.9.2-1/libiconv-1.9.2-1-bin.zip"
$PCRE_URL="$DL_SERVER/pcre/7.0/pcre-7.0-bin.zip"
$REGEX_URL="$DL_SERVER/regex/2.7/regex-2.7-bin.zip"
$OPENSSL_URL="$DL_SERVER/openssl/0.9.8h-1/openssl-0.9.8h-1-bin.zip"
$GAWK_URL="$DL_SERVER/gawk/3.1.6-1/gawk-3.1.6-1-bin.zip"
$WGET_URL="$DL_SERVER/wget/1.11.4-1/wget-1.11.4-1-bin.zip"
$GREP_URL="$DL_SERVER/grep/2.5.4/grep-2.5.4-bin.zip"
$SED_URL="$DL_SERVER/sed/4.2.1/sed-4.2.1-bin.zip"
$UNZIP_URL="$DL_SERVER/unzip/5.51-1/unzip-5.51-1-bin.zip"
$CURL_URL="http://www.paehl.com/open_source/downloads/curl_739_0_ssl.zip"

function Check-And-Download($exec, $url, $target, $destination) {
    Write-Output "Checking $exec"

    if($exec -like "*.dll") {
        $check = Test-Path $gnuwin32Path\bin\$exec
    } else {
        $check = Get-Command $exec -ErrorAction SilentlyContinue
    }
  
    if (-Not $check) {
	Write-Output "$exec does not exist"
        
        if (-Not (Test-Path $target)) {
            Write-Output "$target does not exist and will downlad from $url";
            $webclient = New-Object System.Net.WebClient;
            $webclient.DownloadFile($url, $target);
        }

        Write-Output "Unzip $target to $destination";
        $shell = new-object -com shell.application
        $zip = $shell.NameSpace($target)
   
        foreach($item in $zip.items()){
            $shell.Namespace($destination).copyhere($item, 16)
        }
    } else {
        Write-Output "$exec exists";
    }
}

function Get-ScriptDirectory() {
    $Invocation = (Get-Variable MyInvocation -Scope 1).Value
    Split-Path $Invocation.MyCommand.Path
}

$toolsPath = Get-ScriptDirectory;
$gnuwin32Path = [Environment]::GetEnvironmentVariable("GNUWIN32_PATH");

if (-Not ($gnuwin32Path)) {
    Write-Output "GNUWIN32_PATH not set"
    return
}

if (-Not (Test-Path $gnuwin32Path)) {
    Write-output "$gnuwin32Path does not exist and will create it"
    New-Item -ItemType directory -Path $gnuwin32Path
}


$tools = @(@{"exec"="libintl3.dll";"url"=$LIBINTL_URL;"target"="$toolsPath\libintl.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="libiconv2.dll";"url"=$LIBICONV_URL;"target"="$toolsPath\libiconv.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="pcre3.dll";"url"=$PCRE_URL;"target"="$toolsPath\pcre.zip";"installdir"="$gnuwin32Path"},          
           @{"exec"="regex2.dll";"url"=$REGEX_URL;"target"="$toolsPath\regex.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="libssl32.dll";"url"=$OPENSSL_URL;"target"="$toolsPath\openssl.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="gawk.exe";"url"=$GAWK_URL;"target"="$toolsPath\gawk.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="wget.exe";"url"=$WGET_URL;"target"="$toolsPath\wget.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="grep.exe";"url"=$GREP_URL;"target"="$toolsPath\grep.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="sed.exe";"url"=$SED_URL;"target"="$toolsPath\sed.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="unzip.exe";"url"=$UNZIP_URL;"target"="$toolsPath\unzip.zip";"installdir"="$gnuwin32Path"},
           @{"exec"="curl.exe";"url"=$CURL_URL;"target"="$toolsPath\curl.zip";"installdir"="$gnuwin32Path\bin"})

foreach($tool in $tools) {
    Check-And-Download $tool["exec"] $tool["url"] $tool["target"] $tool["installdir"]
}
