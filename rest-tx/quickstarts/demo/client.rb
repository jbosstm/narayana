require "net/http"
require "uri"
require 'optparse'

def doRequest(http, requri, method, body)
	request = nil

	if method == "Head"
		request = Net::HTTP::Head.new(requri)
	elsif method == "Post"
		request = Net::HTTP::Post.new(requri)
		request["Content-Type"] = "application/x-www-form-urlencoded"
	elsif method == "Put"
		request = Net::HTTP::Put.new(requri)
		request["Content-Type"] = "application/txstatus"
	else
		request = Net::HTTP::Get.new(requri)
		request["Content-Type"] = "text/plain"
	end

	request["Content-Length"] = body.length 
	request.body = body
	response = http.request(request)

	return response
end

def newRequest(url, method = 'Get', body = '', useproxy = 0)
	uri = URI.parse(url)

	if useproxy != "0"
#		print "using proxies\n"
		proxy = Net::HTTP::Proxy('file.rdu.redhat.com', 3128)
		proxy.start(uri.host, uri.port)  do |http|
			doRequest(http, uri.request_uri, method, body)
		end
	else
#		print "no proxies\n"
		http = Net::HTTP.new(uri.host, uri.port)
		doRequest(http, uri.request_uri, method, body)
	end
end

def parseLinkHeaders(response)
	enlistUrl = endUrl = location = nil
	location = response["Location"]
	links = response["link"]
	if (links != nil) then
		links = links.delete " \"<>"
		links.each_line(',') {
  			|link|
    		enlistUrl = link.split(';') [0] if link.include? "rel=durableparticipant"
			endUrl = link.split(';') [0] if link.include? "rel=terminator"
		}
	end

	if (location != nil) then
		y=location[location.rindex('/') + 1 .. location.length]
		File.open('tx', 'w') {|f| f.write(y) }
	end

	return enlistUrl, endUrl, location
end

OPTIONS = {
  :verb  => 'Get',
  :body  => '',
  :proxy  => "0",
}

OptionParser.new do |o|
	o.on('-a url') { |OPTIONS[:url]| }
	o.on('-b body') { |OPTIONS[:body]| }
	o.on('-p <true|false>') { |OPTIONS[:proxy]| }
	o.on('-v method') { |OPTIONS[:verb]| }
	o.on('-t <txn url>') { |OPTIONS[:txn]| }
	o.on('-h') { puts o; exit }
	o.parse!
end

url = OPTIONS[:url]
verb = OPTIONS[:verb]
body = OPTIONS[:body]
txn = OPTIONS[:txn]
proxy = OPTIONS[:proxy]

response = newRequest(url, verb, body, proxy)
if response.code != "200" && response.code != "201"
	print "request error: ", response.code, " url=", url, " verb=", verb, "\n"
else
	print response.body, "\n"
	enlistUrl, endUrl, location = parseLinkHeaders(response)
	print "\nenlistUrl: ", enlistUrl, "\nendUrl: ", endUrl, "\n" if enlistUrl != nil
	print "\nlocation: ", location, "\n" if location != nil
end
