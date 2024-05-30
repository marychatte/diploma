local size = tonumber(os.getenv('WRK_BODY_SIZE'))

wrk.method = "POST"
wrk.body   = ('0'):rep(size)
